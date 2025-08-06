package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.config.DltConfiguration;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltProcessingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strategy for handling DLT messages from critical/high-value accounts
 * Applies immediate escalation and requires manual intervention
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CriticalAccountDltStrategy implements DltProcessingStrategy {

    private final DltConfiguration dltConfiguration;
    private static final String STRATEGY_NAME = "CriticalAccountDltStrategy";

    @Override
    public DltProcessingResult processDltMessage(DltMessageDTO dltMessage) {
        log.error("🔥 CRITICAL ACCOUNT DLT: Processing failed message for high-value account {}",
                dltMessage.originalMessage().accountNumber());

        List<String> actions = List.of(
            "Immediate notification sent to VIP support team",
            "Priority escalation created",
            "Account manager notified",
            "Business impact assessment initiated"
        );

        // For critical accounts, always require manual intervention
        // No automatic retries as failures might indicate systemic issues
        return DltProcessingResult.escalated(
            STRATEGY_NAME,
            String.format("Critical account %s message processing failed: %s",
                dltMessage.originalMessage().accountNumber(),
                dltMessage.errorMessage()),
            actions
        );
    }

    @Override
    public boolean canHandle(DltMessageDTO dltMessage) {
        Long accountNumber = dltMessage.originalMessage().accountNumber();
        Long threshold = dltConfiguration.getCriticalAccount().getAccountThreshold();
        
        log.debug("Account {} critical check: threshold={}", accountNumber, threshold);
        
        return accountNumber != null && accountNumber > threshold;
    }

    @Override
    public int getPriority() {
        return 100; // Highest priority - critical accounts always take precedence
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}