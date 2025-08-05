package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltStrategyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

@DisplayName("DLT Retry Service Enhanced Tests")
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DltRetryServiceEnhancedTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScheduledFuture<Object> scheduledFuture;
    
    @Mock
    private StreamBridge streamBridge;
    
    @Mock
    private Function<AccountsMsgDTO, AccountsMsgDTO> emailProcessor;
    
    @Mock
    private Function<AccountsMsgDTO, Long> smsProcessor;
    
    @Mock
    private DltStrategyManager dltStrategyManager;

    private DltRetryService dltRetryService;

    @BeforeEach
    void setUp() {
        dltRetryService = new DltRetryService(taskScheduler, streamBridge, emailProcessor, smsProcessor, dltStrategyManager);
    }

    @Test
    @DisplayName("재시도 실행 테스트 - 직접 처리 실패 후 재발행 성공")
    void shouldExecuteRetryWithRepublishAfterDirectProcessingFailure(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Temporary failure",
            "SocketTimeoutException",
            LocalDateTime.now(),
            1,
            "Network timeout"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            30000L
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        
        // Mock direct processing failure but successful republish
        when(emailProcessor.apply(originalMessage)).thenThrow(new RuntimeException("Direct processing failed"));
        when(streamBridge.send(eq("emailsms-out-0"), any())).thenReturn(true);

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Executing scheduled retry for account 123456789");
        assertThat(output.getOut()).contains("Direct processing retry failed for account 123456789");
        assertThat(output.getOut()).contains("Message successfully re-published to original topic");
        assertThat(output.getOut()).contains("Retry executed successfully for account 123456789");
        
        verify(streamBridge).send(eq("emailsms-out-0"), any());
    }
    
    @Test
    @DisplayName("재시도 실행 중 모든 전략 실패 테스트")
    void shouldHandleAllRetryStrategiesFailure(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Temporary failure",
            "SocketTimeoutException",
            LocalDateTime.now(),
            1,
            "Network timeout"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            30000L
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        
        // Mock all processing methods to fail
        when(emailProcessor.apply(originalMessage)).thenThrow(new RuntimeException("Email processing failed"));
        when(streamBridge.send(eq("emailsms-out-0"), any())).thenReturn(false);
        
        // Mock strategy manager to return no further retries
        DltProcessingResult noRetryResult = DltProcessingResult.permanentFailure(
            "PermanentErrorDltStrategy",
            "Max retries exceeded",
            null
        );
        when(dltStrategyManager.processDltMessage(any())).thenReturn(noRetryResult);

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Executing scheduled retry for account 123456789");
        assertThat(output.getOut()).contains("All retry strategies failed for account 123456789");
        assertThat(output.getOut()).contains("No more retries scheduled for account 123456789");
        
        verify(dltStrategyManager).processDltMessage(any(DltMessageDTO.class));
    }
    
    @Test
    @DisplayName("재시도 실패 후 추가 재시도 스케줄링 테스트")
    void shouldScheduleAdditionalRetryAfterFailure(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Temporary failure",
            "SocketTimeoutException",
            LocalDateTime.now(),
            1,
            "Network timeout"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            30000L
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        
        // Mock all processing methods to fail
        when(emailProcessor.apply(originalMessage)).thenThrow(new RuntimeException("Processing failed"));
        when(streamBridge.send(eq("emailsms-out-0"), any())).thenReturn(false);
        
        // Mock strategy manager to return another retry
        DltProcessingResult anotherRetryResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Another retry scheduled",
            60000L
        );
        when(dltStrategyManager.processDltMessage(any())).thenReturn(anotherRetryResult);

        // Schedule the initial retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Executing scheduled retry for account 123456789");
        assertThat(output.getOut()).contains("All retry strategies failed for account 123456789");
        assertThat(output.getOut()).contains("Scheduling another retry for account 123456789 with delay 60000ms");
        
        // Verify that another retry was scheduled
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
        verify(dltStrategyManager).processDltMessage(any(DltMessageDTO.class));
    }

    @Test
    @DisplayName("재시도 메트릭스 기록 테스트")
    void shouldRecordRetryMetrics(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(555666777L, "Metrics User", "metrics@test.com", "5556667777");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Service timeout",
            "TimeoutException",
            LocalDateTime.now(),
            2,
            "Service response timeout"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            45000L
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        
        // Mock successful direct processing
        when(emailProcessor.apply(originalMessage)).thenReturn(originalMessage);
        when(smsProcessor.apply(originalMessage)).thenReturn(555666777L);

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Recording retry metrics: account=555666777, outcome=success, attemptCount=2");
        assertThat(output.getOut()).contains("Retry executed successfully for account 555666777");
    }

    @Test
    @DisplayName("크리티컬 에러 처리 및 메트릭스 기록 테스트")
    void shouldHandleCriticalErrorAndRecordMetrics(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(999888777L, "Critical User", "critical@test.com", "9998887777");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Critical system failure",
            "SystemException",
            LocalDateTime.now(),
            3,
            "System down"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            30000L
        );

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doReturn(scheduledFuture).when(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        
        // Mock critical error during execution
        when(emailProcessor.apply(originalMessage)).thenThrow(new OutOfMemoryError("Critical system error"));

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Critical error during retry execution for account 999888777");
        assertThat(output.getOut()).contains("Recording retry metrics: account=999888777, outcome=critical_error, attemptCount=3");
    }
}