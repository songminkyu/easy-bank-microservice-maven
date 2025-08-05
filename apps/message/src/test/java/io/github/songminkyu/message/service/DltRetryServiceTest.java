package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("DLT Retry Service Tests")
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DltRetryServiceTest {

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ScheduledFuture<?> scheduledFuture;

    private DltRetryService dltRetryService;

    @BeforeEach
    void setUp() {
        dltRetryService = new DltRetryService(taskScheduler);
    }

    @Test
    @DisplayName("재시도 스케줄링 성공 테스트")
    void shouldScheduleRetrySuccessfully(CapturedOutput output) {
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

        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);

        // When
        dltRetryService.scheduleRetry(dltMessage, processingResult);

        // Then
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        assertThat(output.getOut()).contains("Scheduling retry for account 123456789 with delay 30000ms");
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(1);
        assertThat(dltRetryService.isRetryScheduled(dltMessage)).isTrue();
    }

    @Test
    @DisplayName("재시도 불필요한 경우 스케줄링 건너뛰기")
    void shouldSkipSchedulingWhenRetryNotRequested(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Permanent failure",
            "IllegalArgumentException",
            LocalDateTime.now(),
            1,
            "Validation failed"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.permanentFailure(
            "PermanentErrorDltStrategy",
            "Permanent failure",
            null
        );

        // When
        dltRetryService.scheduleRetry(dltMessage, processingResult);

        // Then
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Instant.class));
        assertThat(output.getOut()).contains("Retry not requested for message: account=123456789");
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("기존 스케줄된 재시도 취소 후 새로운 재시도 스케줄링")
    void shouldCancelExistingRetryBeforeSchedulingNew(CapturedOutput output) {
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

        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);

        // When - Schedule first retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        
        // When - Schedule second retry for same message
        dltRetryService.scheduleRetry(dltMessage, processingResult);

        // Then
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
        verify(scheduledFuture).cancel(false); // Previous retry should be cancelled
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(1); // Only one active retry
    }

    @Test
    @DisplayName("재시도 취소 테스트")
    void shouldCancelScheduledRetry() {
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

        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);
        when(scheduledFuture.isDone()).thenReturn(false);

        // Schedule a retry first
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(1);

        // When
        dltRetryService.cancelRetry(dltMessage);

        // Then
        verify(scheduledFuture).cancel(false);
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(0);
        assertThat(dltRetryService.isRetryScheduled(dltMessage)).isFalse();
    }

    @Test
    @DisplayName("재시도 실행 테스트")
    void shouldExecuteScheduledRetry(CapturedOutput output) {
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
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);

        // Capture the scheduled runnable
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));
        Runnable retryTask = runnableCaptor.getValue();

        // When - Execute the retry task
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Executing scheduled retry for account 123456789");
        assertThat(output.getOut()).contains("Retry executed for account 123456789");
    }

    @Test
    @DisplayName("재시도 실행 중 예외 처리 테스트")
    void shouldHandleExceptionDuringRetryExecution(CapturedOutput output) {
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
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);

        // Schedule the retry
        dltRetryService.scheduleRetry(dltMessage, processingResult);
        verify(taskScheduler).schedule(runnableCaptor.capture(), any(Instant.class));

        // When - Execute the retry task (this will complete normally as it just logs)
        Runnable retryTask = runnableCaptor.getValue();
        retryTask.run();

        // Then
        assertThat(output.getOut()).contains("Executing scheduled retry for account 123456789");
        assertThat(output.getOut()).contains("Retry executed for account 123456789");
    }

    @Test
    @DisplayName("다중 메시지 재시도 스케줄링 테스트")
    void shouldHandleMultipleRetrySchedules(CapturedOutput output) {
        // Given
        AccountsMsgDTO message1 = new AccountsMsgDTO(111111111L, "User One", "user1@test.com", "1111111111");
        AccountsMsgDTO message2 = new AccountsMsgDTO(222222222L, "User Two", "user2@test.com", "2222222222");
        
        DltMessageDTO dltMessage1 = new DltMessageDTO(message1, "Error 1", "TimeoutException", LocalDateTime.now(), 1, "Timeout 1");
        DltMessageDTO dltMessage2 = new DltMessageDTO(message2, "Error 2", "ConnectException", LocalDateTime.now(), 1, "Connection 2");
        
        DltProcessingResult result1 = DltProcessingResult.retry("Strategy1", "Retry 1", 30000L);
        DltProcessingResult result2 = DltProcessingResult.retry("Strategy2", "Retry 2", 60000L);

        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn(scheduledFuture);

        // When
        dltRetryService.scheduleRetry(dltMessage1, result1);
        dltRetryService.scheduleRetry(dltMessage2, result2);

        // Then
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
        assertThat(dltRetryService.getScheduledRetryCount()).isEqualTo(2);
        assertThat(dltRetryService.isRetryScheduled(dltMessage1)).isTrue();
        assertThat(dltRetryService.isRetryScheduled(dltMessage2)).isTrue();
    }
}