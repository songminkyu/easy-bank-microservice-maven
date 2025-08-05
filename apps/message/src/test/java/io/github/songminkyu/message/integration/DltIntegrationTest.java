package io.github.songminkyu.message.integration;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.monitoring.DltMetrics;
import io.github.songminkyu.message.service.DltAlertService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("DLT Integration Tests")
@ExtendWith(OutputCaptureExtension.class)
class DltIntegrationTest {

    @Autowired
    private DltAlertService dltAlertService;

    @Autowired
    private DltMetrics dltMetrics;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("일반 DLT 메시지 처리 통합 테스트")
    void shouldProcessNormalDltMessage(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Integration Test User", "integration@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Integration test error",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Test failure"
        );

        // When
        Timer.Sample timerSample = dltMetrics.startDltProcessingTimer();
        dltMetrics.recordDltMessage(dltMessage);
        dltAlertService.sendDltAlert(dltMessage);
        dltMetrics.recordDltProcessingTime(timerSample, "success");

        // Then
        assertThat(output.getOut()).contains("DLT ALERT: Message processing failed for account 123456789 after 2 attempts");
        assertThat(output.getOut()).contains("DLT metric recorded for account: 123456789");
        assertThat(output.getOut()).contains("DLT alert notifications sent for account: 123456789");
        
        // 메트릭이 제대로 등록되었는지 확인
        assertThat(meterRegistry.counter("message.dlt.received", 
            "exception.class", "RuntimeException", 
            "account.number", "123456789").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("크리티컬 DLT 메시지 처리 통합 테스트")
    void shouldProcessCriticalDltMessage(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(1000000001L, "VIP Integration Test", "vip@test.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Critical integration test error",
            "NullPointerException",
            LocalDateTime.now(),
            5,
            "Critical system failure"
        );

        // When
        Timer.Sample timerSample = dltMetrics.startDltProcessingTimer();
        dltMetrics.recordDltMessage(dltMessage);
        
        if (dltAlertService.isCriticalFailure(dltMessage)) {
            dltAlertService.sendCriticalAlert(dltMessage);
            dltMetrics.recordDltProcessingTime(timerSample, "critical");
        } else {
            dltAlertService.sendDltAlert(dltMessage);
            dltMetrics.recordDltProcessingTime(timerSample, "normal");
        }

        // Then
        assertThat(output.getOut()).contains("CRITICAL DLT ALERT: High-priority message failed for account 1000000001");
        assertThat(output.getOut()).contains("IMMEDIATE ALERT: Critical message failure for account 1000000001");
        assertThat(output.getOut()).contains("ESCALATED: Critical DLT message escalated to operations team for account 1000000001");
        assertThat(output.getOut()).contains("DLT metric recorded for account: 1000000001");
        
        // 메트릭이 제대로 등록되었는지 확인
        assertThat(meterRegistry.counter("message.dlt.received", 
            "exception.class", "NullPointerException", 
            "account.number", "1000000001").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("메시지 처리 실패 후 DLT 처리 플로우 통합 테스트")
    void shouldHandleMessageProcessingFailureFlow(CapturedOutput output) {
        // Given
        String messageType = "account.created";
        String errorType = "validation.failed";
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(555666777L, "Failure Test User", "failure@test.com", "5556667777");
        RuntimeException processingException = new RuntimeException("Validation failed: missing required field");

        // When - 메시지 처리 실패 시뮬레이션
        dltMetrics.recordMessageFailure(messageType, errorType);

        // DLT로 메시지 전송
        DltMessageDTO dltMessage = DltMessageDTO.from(originalMessage, processingException, 1);
        Timer.Sample timerSample = dltMetrics.startDltProcessingTimer();
        dltMetrics.recordDltMessage(dltMessage);
        dltAlertService.sendDltAlert(dltMessage);
        dltMetrics.recordDltProcessingTime(timerSample, "processed");

        // Then
        assertThat(output.getOut()).contains("Message failure metric recorded: type=account.created, error=validation.failed");
        assertThat(output.getOut()).contains("DLT ALERT: Message processing failed for account 555666777 after 1 attempts");
        assertThat(output.getOut()).contains("DLT metric recorded for account: 555666777");
        
        // 실패 메트릭이 기록되었는지 확인
        assertThat(meterRegistry.counter("message.processing.failures", 
            "message.type", messageType, 
            "error.type", errorType).count()).isGreaterThan(0);
        
        // DLT 메트릭이 기록되었는지 확인
        assertThat(meterRegistry.counter("message.dlt.received", 
            "exception.class", "RuntimeException", 
            "account.number", "555666777").count()).isGreaterThan(0);
    }

    @Test
    @DisplayName("경계값 조건에서의 크리티컬 판단 통합 테스트")
    void shouldHandleBoundaryConditionsInCriticalDetection(CapturedOutput output) {
        // Given - 경계값 테스트 (정확히 3회 시도)
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(777888999L, "Boundary Test User", "boundary@test.com", "7778889999");
        DltMessageDTO boundaryMessage = new DltMessageDTO(
            originalMessage,
            "Boundary condition test",
            "TimeoutException",
            LocalDateTime.now(),
            3, // 정확히 3회 시도
            "Connection timeout"
        );

        // When
        Timer.Sample timerSample = dltMetrics.startDltProcessingTimer();
        dltMetrics.recordDltMessage(boundaryMessage);
        
        if (dltAlertService.isCriticalFailure(boundaryMessage)) {
            dltAlertService.sendCriticalAlert(boundaryMessage);
            dltMetrics.recordDltProcessingTime(timerSample, "critical");
        } else {
            dltAlertService.sendDltAlert(boundaryMessage);
            dltMetrics.recordDltProcessingTime(timerSample, "normal");
        }

        // Then - 3회 시도는 크리티컬 조건(>=3)에 해당하므로 크리티컬 알림이 발생해야 함
        assertThat(dltAlertService.isCriticalFailure(boundaryMessage)).isTrue();
        assertThat(output.getOut()).contains("CRITICAL DLT ALERT: High-priority message failed for account 777888999");
    }

    @Test
    @DisplayName("복수의 DLT 메시지 동시 처리 통합 테스트")
    void shouldHandleMultipleDltMessages(CapturedOutput output) {
        // Given
        AccountsMsgDTO message1 = new AccountsMsgDTO(111111111L, "User One", "user1@test.com", "1111111111");
        AccountsMsgDTO message2 = new AccountsMsgDTO(222222222L, "User Two", "user2@test.com", "2222222222");
        
        DltMessageDTO dltMessage1 = new DltMessageDTO(message1, "Error 1", "Exception1", LocalDateTime.now(), 1, "Reason 1");
        DltMessageDTO dltMessage2 = new DltMessageDTO(message2, "Error 2", "Exception2", LocalDateTime.now(), 2, "Reason 2");

        // When
        Timer.Sample timer1 = dltMetrics.startDltProcessingTimer();
        Timer.Sample timer2 = dltMetrics.startDltProcessingTimer();
        
        dltMetrics.recordDltMessage(dltMessage1);
        dltMetrics.recordDltMessage(dltMessage2);
        
        dltAlertService.sendDltAlert(dltMessage1);
        dltAlertService.sendDltAlert(dltMessage2);
        
        dltMetrics.recordDltProcessingTime(timer1, "success");
        dltMetrics.recordDltProcessingTime(timer2, "success");

        // Then
        assertThat(output.getOut()).contains("Message processing failed for account 111111111 after 1 attempts");
        assertThat(output.getOut()).contains("Message processing failed for account 222222222 after 2 attempts");
        
        // 각각의 메트릭이 기록되었는지 확인
        assertThat(meterRegistry.counter("message.dlt.received", 
            "exception.class", "Exception1", 
            "account.number", "111111111").count()).isGreaterThan(0);
        assertThat(meterRegistry.counter("message.dlt.received", 
            "exception.class", "Exception2", 
            "account.number", "222222222").count()).isGreaterThan(0);
    }
}