package io.github.songminkyu.message.monitoring;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DLT Metrics Tests")
class DltMetricsTest {

    private MeterRegistry meterRegistry;
    private DltMetrics dltMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        dltMetrics = new DltMetrics(meterRegistry);
    }

    @Test
    @DisplayName("DLT 메시지 메트릭 기록 테스트")
    void shouldRecordDltMessage() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Processing error",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Database timeout"
        );

        // When
        dltMetrics.recordDltMessage(dltMessage);

        // Then
        double count = meterRegistry.counter("message.dlt.received", 
            "exception.class", "RuntimeException", 
            "account.number", "123456789").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("DLT 처리 타이머 시작 테스트")
    void shouldStartDltProcessingTimer() {
        // When
        Timer.Sample sample = dltMetrics.startDltProcessingTimer();

        // Then
        assertThat(sample).isNotNull();
    }

    @Test
    @DisplayName("DLT 처리 시간 기록 테스트")
    void shouldRecordDltProcessingTime() {
        // Given
        String outcome = "success";
        Timer.Sample sample = dltMetrics.startDltProcessingTimer();

        // When
        dltMetrics.recordDltProcessingTime(sample, outcome);

        // Then
        Timer timer = meterRegistry.timer("message.dlt.processing.time", "outcome", "success");
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("메시지 처리 실패 메트릭 기록 테스트")
    void shouldRecordMessageFailure() {
        // Given
        String messageType = "account.created";
        String errorType = "validation.error";

        // When
        dltMetrics.recordMessageFailure(messageType, errorType);

        // Then
        double count = meterRegistry.counter("message.processing.failures", 
            "message.type", "account.created", 
            "error.type", "validation.error").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("다양한 예외 타입에 대한 DLT 메트릭 기록 테스트")
    void shouldRecordDltMessageWithDifferentExceptionTypes() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(987654321L, "Jane Doe", "jane@example.com", "9876543210");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Null pointer exception",
            "NullPointerException",
            LocalDateTime.now(),
            1,
            "Unexpected null value"
        );

        // When
        dltMetrics.recordDltMessage(dltMessage);

        // Then
        double count = meterRegistry.counter("message.dlt.received", 
            "exception.class", "NullPointerException", 
            "account.number", "987654321").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("처리 실패 후 성공 결과로 DLT 처리 시간 기록")
    void shouldRecordProcessingTimeWithFailureOutcome() {
        // Given
        String outcome = "failure";
        Timer.Sample sample = dltMetrics.startDltProcessingTimer();

        // When
        dltMetrics.recordDltProcessingTime(sample, outcome);

        // Then
        Timer timer = meterRegistry.timer("message.dlt.processing.time", "outcome", "failure");
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("고액 계좌에 대한 DLT 메트릭 기록 테스트")
    void shouldRecordDltMessageForHighValueAccount() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(1000000001L, "VIP Customer", "vip@example.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "High value account processing error",
            "ServiceException",
            LocalDateTime.now(),
            3,
            "Service unavailable"
        );

        // When
        dltMetrics.recordDltMessage(dltMessage);

        // Then
        double count = meterRegistry.counter("message.dlt.received", 
            "exception.class", "ServiceException", 
            "account.number", "1000000001").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("다양한 메시지 타입과 에러 타입으로 실패 메트릭 기록")
    void shouldRecordDifferentMessageAndErrorTypes() {
        // Given
        String messageType1 = "account.updated";
        String errorType1 = "database.error";
        String messageType2 = "account.deleted";
        String errorType2 = "permission.denied";

        // When
        dltMetrics.recordMessageFailure(messageType1, errorType1);
        dltMetrics.recordMessageFailure(messageType2, errorType2);

        // Then
        double count1 = meterRegistry.counter("message.processing.failures", 
            "message.type", "account.updated", 
            "error.type", "database.error").count();
        double count2 = meterRegistry.counter("message.processing.failures", 
            "message.type", "account.deleted", 
            "error.type", "permission.denied").count();
        
        assertThat(count1).isEqualTo(1.0);
        assertThat(count2).isEqualTo(1.0);
    }

    @Test
    @DisplayName("계좌 번호가 null인 경우 메트릭 기록 테스트")
    void shouldHandleNullAccountNumberInMetrics() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(null, "No Account", "nouser@example.com", "0000000000");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Account validation error",
            "IllegalArgumentException",
            LocalDateTime.now(),
            1,
            "Missing account number"
        );

        // When
        dltMetrics.recordDltMessage(dltMessage);

        // Then
        double count = meterRegistry.counter("message.dlt.received", 
            "exception.class", "IllegalArgumentException", 
            "account.number", "null").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("복수의 DLT 메시지 기록 테스트")
    void shouldRecordMultipleDltMessages() {
        // Given
        AccountsMsgDTO message1 = new AccountsMsgDTO(111111111L, "User One", "user1@test.com", "1111111111");
        AccountsMsgDTO message2 = new AccountsMsgDTO(222222222L, "User Two", "user2@test.com", "2222222222");
        
        DltMessageDTO dltMessage1 = new DltMessageDTO(message1, "Error 1", "Exception1", LocalDateTime.now(), 1, "Reason 1");
        DltMessageDTO dltMessage2 = new DltMessageDTO(message2, "Error 2", "Exception2", LocalDateTime.now(), 2, "Reason 2");

        // When
        dltMetrics.recordDltMessage(dltMessage1);
        dltMetrics.recordDltMessage(dltMessage2);

        // Then
        double count1 = meterRegistry.counter("message.dlt.received", 
            "exception.class", "Exception1", 
            "account.number", "111111111").count();
        double count2 = meterRegistry.counter("message.dlt.received", 
            "exception.class", "Exception2", 
            "account.number", "222222222").count();
        
        assertThat(count1).isEqualTo(1.0);
        assertThat(count2).isEqualTo(1.0);
    }

    @Test
    @DisplayName("동일한 메트릭에 대한 여러 번 호출 테스트")
    void shouldAccumulateMetricsForSameCounter() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Repeated error",
            "RuntimeException",
            LocalDateTime.now(),
            1,
            "Same error occurred"
        );

        // When
        dltMetrics.recordDltMessage(dltMessage);
        dltMetrics.recordDltMessage(dltMessage); // 같은 메시지 두 번 기록

        // Then
        double count = meterRegistry.counter("message.dlt.received", 
            "exception.class", "RuntimeException", 
            "account.number", "123456789").count();
        assertThat(count).isEqualTo(2.0); // 두 번 호출되었으므로 2.0
    }
}