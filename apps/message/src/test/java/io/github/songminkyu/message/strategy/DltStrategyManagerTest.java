package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.config.DltConfiguration;
import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.impl.CriticalAccountDltStrategy;
import io.github.songminkyu.message.strategy.impl.DefaultDltStrategy;
import io.github.songminkyu.message.strategy.impl.PermanentErrorDltStrategy;
import io.github.songminkyu.message.strategy.impl.TransientErrorDltStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("DLT Strategy Manager Tests")
@ExtendWith(MockitoExtension.class)
class DltStrategyManagerTest {

    @Mock
    private DltConfiguration dltConfiguration;

    @Mock
    private DltConfiguration.RetryStrategy retryStrategyConfig;

    @Mock
    private DltConfiguration.PermanentError permanentErrorConfig;

    private DltStrategyManager dltStrategyManager;

    @BeforeEach
    void setUp() {
        // Setup default mock behaviors
        when(dltConfiguration.getRetryStrategy()).thenReturn(retryStrategyConfig);
        when(dltConfiguration.getPermanentError()).thenReturn(permanentErrorConfig);
        
        when(retryStrategyConfig.getMaxDltRetryAttempts()).thenReturn(2);
        when(retryStrategyConfig.getBaseRetryDelayMs()).thenReturn(30000L);
        when(retryStrategyConfig.getBackoffMultiplier()).thenReturn(2.0);
        when(retryStrategyConfig.getMaxRetryDelayMs()).thenReturn(300000L);
        when(retryStrategyConfig.getTransientErrorTypes()).thenReturn(new String[]{
            "ConnectException", "SocketTimeoutException", "TimeoutException"
        });

        when(permanentErrorConfig.getPermanentErrorTypes()).thenReturn(new String[]{
            "IllegalArgumentException", "ValidationException"
        });
        when(permanentErrorConfig.getPermanentErrorMessages()).thenReturn(new String[]{
            "cannot be null", "invalid format"
        });

        // Create strategies with mocked configuration
        List<DltProcessingStrategy> strategies = List.of(
            new CriticalAccountDltStrategy(),
            new TransientErrorDltStrategy(dltConfiguration),
            new PermanentErrorDltStrategy(),
            new DefaultDltStrategy()
        );

        dltStrategyManager = new DltStrategyManager(strategies);
    }

    @Test
    @DisplayName("크리티컬 계좌는 CriticalAccountDltStrategy 선택")
    void shouldSelectCriticalAccountStrategyForHighValueAccount() {
        // Given
        AccountsMsgDTO criticalAccount = new AccountsMsgDTO(1500000000L, "VIP Customer", "vip@test.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            criticalAccount,
            "Service failure",
            "RuntimeException",
            LocalDateTime.now(),
            1,
            "Service unavailable"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then
        assertThat(result.strategyUsed()).isEqualTo("CriticalAccountDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.ESCALATED);
        assertThat(result.requiresManualIntervention()).isTrue();
    }

    @Test
    @DisplayName("일시적 에러는 TransientErrorDltStrategy 선택")
    void shouldSelectTransientErrorStrategyForRetryableErrors() {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Connection timeout",
            "SocketTimeoutException",
            LocalDateTime.now(),
            1,
            "Network timeout"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then
        assertThat(result.strategyUsed()).isEqualTo("TransientErrorDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.RETRY_SCHEDULED);
        assertThat(result.shouldRetry()).isTrue();
        assertThat(result.retryDelayMs()).isEqualTo(30000L); // Base delay for first retry
    }

    @Test
    @DisplayName("영구적 에러는 PermanentErrorDltStrategy 선택")
    void shouldSelectPermanentErrorStrategyForNonRetryableErrors() {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Account number cannot be null",
            "IllegalArgumentException",
            LocalDateTime.now(),
            1,
            "Validation failed"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then
        assertThat(result.strategyUsed()).isEqualTo("PermanentErrorDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.PERMANENT_FAILURE);
        assertThat(result.shouldRetry()).isFalse();
    }

    @Test
    @DisplayName("특별한 조건이 없으면 DefaultDltStrategy 선택")
    void shouldSelectDefaultStrategyForStandardMessages() {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Unknown processing error",
            "RuntimeException",
            LocalDateTime.now(),
            2,
            "Processing failed"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then
        assertThat(result.strategyUsed()).isEqualTo("DefaultDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.SUCCESS);
    }

    @Test
    @DisplayName("전략 우선순위에 따른 선택 테스트")
    void shouldSelectHighestPriorityStrategy() {
        // Given - Critical account with transient error (both strategies can handle)
        AccountsMsgDTO criticalAccount = new AccountsMsgDTO(2000000000L, "VIP Customer", "vip@test.com", "9999999999");
        DltMessageDTO dltMessage = new DltMessageDTO(
            criticalAccount,
            "Connection timeout",
            "SocketTimeoutException",
            LocalDateTime.now(),
            1,
            "Network timeout"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then - CriticalAccountDltStrategy should be selected due to higher priority (100 vs 80)
        assertThat(result.strategyUsed()).isEqualTo("CriticalAccountDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.ESCALATED);
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 시 수동 개입 필요")
    void shouldRequireManualInterventionAfterMaxRetries() {    
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        DltMessageDTO dltMessage = new DltMessageDTO(
            normalAccount,
            "Connection timeout",
            "SocketTimeoutException",
            LocalDateTime.now(),
            3, // Exceeds max retry attempts (2)
            "Network timeout"
        );

        // When
        DltProcessingResult result = dltStrategyManager.processDltMessage(dltMessage);

        // Then
        assertThat(result.strategyUsed()).isEqualTo("TransientErrorDltStrategy");
        assertThat(result.status()).isEqualTo(DltProcessingResult.ProcessingStatus.MANUAL_INTERVENTION_REQUIRED);
        assertThat(result.requiresManualIntervention()).isTrue();
        assertThat(result.shouldRetry()).isFalse();
    }

    @Test
    @DisplayName("사용 가능한 전략 정보 조회")
    void shouldReturnAvailableStrategiesInfo() {
        // When
        List<DltStrategyManager.StrategyInfo> strategies = dltStrategyManager.getAvailableStrategies();

        // Then
        assertThat(strategies).hasSize(4);
        assertThat(strategies.get(0).name()).isEqualTo("CriticalAccountDltStrategy"); // Highest priority first
        assertThat(strategies.get(0).priority()).isEqualTo(100);
        assertThat(strategies.get(3).name()).isEqualTo("DefaultDltStrategy"); // Lowest priority last
        assertThat(strategies.get(3).priority()).isEqualTo(10);
    }

    @Test
    @DisplayName("지수 백오프 지연 시간 계산 테스트")
    void shouldCalculateExponentialBackoffCorrectly() {
        // Given
        AccountsMsgDTO normalAccount = new AccountsMsgDTO(123456789L, "Normal User", "user@test.com", "1234567890");
        
        // First retry attempt
        DltMessageDTO firstRetry = new DltMessageDTO(
            normalAccount, "Timeout", "SocketTimeoutException", LocalDateTime.now(), 0, "Network timeout"
        );
        
        // Second retry attempt  
        DltMessageDTO secondRetry = new DltMessageDTO(
            normalAccount, "Timeout", "SocketTimeoutException", LocalDateTime.now(), 1, "Network timeout"
        );

        // When
        DltProcessingResult firstResult = dltStrategyManager.processDltMessage(firstRetry);
        DltProcessingResult secondResult = dltStrategyManager.processDltMessage(secondRetry);

        // Then
        assertThat(firstResult.retryDelayMs()).isEqualTo(30000L); // 30 seconds (base delay)
        assertThat(secondResult.retryDelayMs()).isEqualTo(60000L); // 60 seconds (30 * 2^1)
    }
}