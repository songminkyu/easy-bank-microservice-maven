package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.impl.CriticalAlertStrategy;
import io.github.songminkyu.message.strategy.impl.StandardAlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Alert Strategy Manager Tests")
@ExtendWith(OutputCaptureExtension.class)
class AlertStrategyManagerTest {

    private AlertStrategyManager alertStrategyManager;

    @BeforeEach
    void setUp() {
        List<AlertStrategy> alertStrategies = List.of(
            new CriticalAlertStrategy(),
            new StandardAlertStrategy()
        );
        alertStrategyManager = new AlertStrategyManager(alertStrategies);
    }

    @Test
    @DisplayName("크리티컬 상황에서 CriticalAlertStrategy 선택")
    void shouldSelectCriticalAlertStrategyForCriticalSituations(CapturedOutput output) {
        // Given
        AccountsMsgDTO criticalAccount = new AccountsMsgDTO(1500000000L, "VIP Customer", "vip@test.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            criticalAccount,
            "Critical system failure",
            "NullPointerException",
            LocalDateTime.now(),
            5,
            "System unavailable"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.escalated(
            "CriticalAccountDltStrategy",
            "Critical failure detected",
            List.of("Immediate action required")
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then
        assertThat(output.getOut()).contains("Selected alert strategy 'CriticalAlertStrategy'");
        assertThat(output.getOut()).contains("CRITICAL ALERT: Immediate attention required for account 1500000000");
        assertThat(output.getOut()).contains("PagerDuty alert triggered");
        assertThat(output.getOut()).contains("Urgent Slack notification sent");
        assertThat(output.getOut()).contains("On-call team email sent");
        assertThat(output.getOut()).contains("Urgent Jira ticket created");
    }

    @Test
    @DisplayName("일반 상황에서 StandardAlertStrategy 선택")
    void shouldSelectStandardAlertStrategyForNormalSituations(CapturedOutput output) {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Processing error",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Temporary failure"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.success(
            "DefaultDltStrategy",
            "Processing completed"
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then
        assertThat(output.getOut()).contains("Selected alert strategy 'StandardAlertStrategy'");
        assertThat(output.getOut()).contains("STANDARD ALERT: DLT processing notification for account 123456789");
        assertThat(output.getOut()).contains("Slack notification sent");
        assertThat(output.getOut()).contains("Email notification sent");
    }

    @Test
    @DisplayName("다중 조건 만족 시 우선순위에 따른 전략 선택")
    void shouldSelectHighestPriorityAlertStrategy(CapturedOutput output) {
        // Given - High-value account with escalated status (both strategies can handle)
        AccountsMsgDTO highValueAccount = new AccountsMsgDTO(2000000000L, "Premium Customer", "premium@test.com", "8888888888");
        DltMessageDTO dltMessage = new DltMessageDTO(
            highValueAccount,
            "Service degradation",
            "ServiceException",
            LocalDateTime.now(),
            3,
            "Multiple failures"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.escalated(
            "CriticalAccountDltStrategy",
            "Multiple failures detected",
            List.of("Investigation required")
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then - CriticalAlertStrategy should be selected due to higher priority (100 vs 50)
        assertThat(output.getOut()).contains("Selected alert strategy 'CriticalAlertStrategy'");
        assertThat(output.getOut()).contains("CRITICAL ALERT");
    }

    @Test
    @DisplayName("수동 개입 필요한 경우 적절한 티켓 생성")
    void shouldCreateTrackingTicketForManualIntervention(CapturedOutput output) {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(555666777L, "Test User", "test@test.com", "5556667777");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Manual review needed",
            "ValidationException",
            LocalDateTime.now(),
            1,
            "Data integrity issue"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.manualIntervention(
            "PermanentErrorDltStrategy",
            "Manual intervention required",
            List.of("Data review needed", "Validation failed")
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then
        assertThat(output.getOut()).contains("Selected alert strategy 'StandardAlertStrategy'");
        assertThat(output.getOut()).contains("Tracking ticket created for account: 555666777");
    }

    @Test
    @DisplayName("재시도 예정 메시지의 알림 처리")
    void shouldHandleRetryScheduledMessages(CapturedOutput output) {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(111222333L, "Retry User", "retry@test.com", "1112223333");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Temporary service unavailable",
            "ServiceUnavailableException",
            LocalDateTime.now(),
            1,
            "Service down"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.retry(
            "TransientErrorDltStrategy",
            "Retry scheduled",
            60000L
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then
        assertThat(output.getOut()).contains("Selected alert strategy 'StandardAlertStrategy'");
        assertThat(output.getOut()).contains("STANDARD ALERT: DLT processing notification for account 111222333");
    }

    @Test
    @DisplayName("사용 가능한 알림 전략 정보 조회")
    void shouldReturnAvailableAlertStrategiesInfo() {
        // When
        List<AlertStrategyManager.AlertStrategyInfo> strategies = alertStrategyManager.getAvailableAlertStrategies();

        // Then
        assertThat(strategies).hasSize(2);
        assertThat(strategies.get(0).name()).isEqualTo("CriticalAlertStrategy"); // Highest priority first
        assertThat(strategies.get(0).priority()).isEqualTo(100);
        assertThat(strategies.get(1).name()).isEqualTo("StandardAlertStrategy"); // Lower priority second
        assertThat(strategies.get(1).priority()).isEqualTo(50);
    }

    @Test
    @DisplayName("영구 실패 메시지의 알림 처리")
    void shouldHandlePermanentFailureMessages(CapturedOutput output) {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(444555666L, "Failed User", "failed@test.com", "4445556666");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Invalid data format",
            "IllegalArgumentException",
            LocalDateTime.now(),
            1,
            "Data corruption detected"
        );
        
        DltProcessingResult processingResult = DltProcessingResult.permanentFailure(
            "PermanentErrorDltStrategy",
            "Permanent failure - message archived",
            List.of("Message archived", "Data corruption logged")
        );

        // When
        alertStrategyManager.sendAlert(dltMessage, processingResult);

        // Then
        assertThat(output.getOut()).contains("Selected alert strategy 'StandardAlertStrategy'");
        assertThat(output.getOut()).contains("STANDARD ALERT: DLT processing notification for account 444555666");
    }
}