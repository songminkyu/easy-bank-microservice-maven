package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DLT Alert Service Tests")
@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DltAlertServiceTest {

    private DltAlertService dltAlertService;

    @BeforeEach
    void setUp() {
        dltAlertService = new DltAlertService();
    }

    @Test
    @DisplayName("일반 DLT 알림 전송 테스트")
    void shouldSendDltAlert(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Processing failed",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Database connection timeout"
        );

        // When
        dltAlertService.sendDltAlert(dltMessage);

        // Then
        assertThat(output.getOut()).contains("DLT ALERT: Message processing failed for account 123456789 after 2 attempts");
        assertThat(output.getOut()).contains("Slack notification sent for DLT message: account=123456789");
        assertThat(output.getOut()).contains("Email alert sent for DLT message: account=123456789");
        assertThat(output.getOut()).contains("Jira ticket created for DLT message: account=123456789");
        assertThat(output.getOut()).contains("DLT alert notifications sent for account: 123456789");
    }

    @Test
    @DisplayName("크리티컬 DLT 알림 전송 테스트")
    void shouldSendCriticalAlert(CapturedOutput output) {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(1000000001L, "VIP Customer", "vip@example.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Critical system failure",
            "NullPointerException",
            LocalDateTime.now(),
            5,
            "System unavailable"
        );

        // When
        dltAlertService.sendCriticalAlert(dltMessage);

        // Then
        assertThat(output.getOut()).contains("CRITICAL DLT ALERT: High-priority message failed for account 1000000001");
        assertThat(output.getOut()).contains("IMMEDIATE ALERT: Critical message failure for account 1000000001");
        assertThat(output.getOut()).contains("ESCALATED: Critical DLT message escalated to operations team for account 1000000001");
        assertThat(output.getOut()).contains("Critical DLT alert sent for account: 1000000001");
    }

    @Test
    @DisplayName("시도 횟수가 3회 이상인 경우 크리티컬 실패로 판단")
    void shouldIdentifyCriticalFailureBasedOnAttemptCount() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Too many attempts",
            "RuntimeException",
            LocalDateTime.now(),
            3,
            "Multiple failures"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isTrue();
    }

    @Test
    @DisplayName("NullPointerException인 경우 크리티컬 실패로 판단")
    void shouldIdentifyCriticalFailureBasedOnExceptionType() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Null pointer error",
            "NullPointerException",
            LocalDateTime.now(),
            1,
            "Unexpected null value"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isTrue();
    }

    @Test
    @DisplayName("고액 계좌(10억 이상)인 경우 크리티컬 실패로 판단")
    void shouldIdentifyCriticalFailureBasedOnHighValueAccount() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(1000000001L, "VIP Customer", "vip@example.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "High value account error",
            "RuntimeException",
            LocalDateTime.now(),
            1,
            "Service failure"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isTrue();
    }

    @Test
    @DisplayName("일반적인 경우는 크리티컬 실패가 아님")
    void shouldNotIdentifyNormalFailureAsCritical() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "John Doe", "john@example.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Normal processing error",
            "RuntimeException",
            LocalDateTime.now(),
            1,
            "Temporary failure"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isFalse();
    }

    @Test
    @DisplayName("복합 조건에서 크리티컬 실패 판단 테스트")
    void shouldHandleMultipleCriticalConditions() {
        // Given - 시도 횟수 5회 + NullPointerException + 고액 계좌
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(2000000000L, "Premium Customer", "premium@example.com", "8888888888");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Multiple critical conditions",
            "NullPointerException",
            LocalDateTime.now(),
            5,
            "System completely failed"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isTrue();
    }

    @Test
    @DisplayName("계좌 번호가 null인 경우 예외 처리 테스트")
    void shouldHandleNullAccountNumber() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(null, "No Account", "nouser@example.com", "0000000000");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Account number is null",
            "IllegalArgumentException",
            LocalDateTime.now(),
            1,
            "Missing account information"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isFalse(); // null 계좌는 고액 계좌 조건에 해당하지 않음
    }

    @Test
    @DisplayName("경계값 테스트 - 정확히 10억인 계좌")
    void shouldHandleBoundaryValueForHighValueAccount() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(1000000000L, "Boundary Customer", "boundary@example.com", "7777777777");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Boundary test",
            "RuntimeException",
            LocalDateTime.now(),
            1,
            "Boundary condition"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isFalse(); // 정확히 10억은 크리티컬 조건(>10억)에 해당하지 않음
    }

    @Test
    @DisplayName("경계값 테스트 - 시도 횟수 2회")
    void shouldHandleBoundaryValueForAttemptCount() {
        // Given
        AccountsMsgDTO originalMessage = new AccountsMsgDTO(123456789L, "Test User", "test@example.com", "1111111111");
        DltMessageDTO dltMessage = new DltMessageDTO(
            originalMessage,
            "Boundary test",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Two attempts made"
        );

        // When
        boolean isCritical = dltAlertService.isCriticalFailure(dltMessage);

        // Then
        assertThat(isCritical).isFalse(); // 2회는 크리티컬 조건(>=3)에 해당하지 않음
    }
}