package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default strategy for handling DLT messages that don't match specific criteria
 * Provides standard logging, alerting, and analysis
 */
@Component
@Slf4j
public class DefaultDltStrategy implements DltProcessingStrategy {

    private static final String STRATEGY_NAME = "DefaultDltStrategy";
    private static final int MAX_DEFAULT_ATTEMPTS = 5;

    @Override
    public DltProcessingResult processDltMessage(DltMessageDTO dltMessage) {
        log.warn("📋 DEFAULT DLT: Processing message with standard strategy for account {}",
                dltMessage.originalMessage().accountNumber());

        int attemptCount = dltMessage.attemptCount();
        
        // Standard processing based on attempt count
        if (attemptCount <= 3) {
            // Normal alert level
            return DltProcessingResult.success(
                STRATEGY_NAME,
                String.format("Standard DLT processing completed for account %s after %d attempts",
                    dltMessage.originalMessage().accountNumber(),
                    attemptCount)
            ).withAction("Standard alert notifications sent")
             .withAction("Metrics recorded")
             .withAction("Log entry created");
             
        } else if (attemptCount <= MAX_DEFAULT_ATTEMPTS) {
            // Elevated concern level
            return DltProcessingResult.manualIntervention(
                STRATEGY_NAME,
                String.format("Elevated concern: Account %s failed %d times",
                    dltMessage.originalMessage().accountNumber(),
                    attemptCount),
                List.of(
                    "Elevated alert level triggered",
                    "Pattern analysis recommended",
                    "Service health check suggested"
                )
            );
        } else {
            // Maximum attempts exceeded - escalate
            return DltProcessingResult.escalated(
                STRATEGY_NAME,
                String.format("Maximum attempts exceeded for account %s (%d attempts)",
                    dltMessage.originalMessage().accountNumber(),
                    attemptCount),
                List.of(
                    "Operations team notified",
                    "System stability assessment required",
                    "Potential service degradation alert"
                )
            );
        }
    }

    @Override
    public boolean canHandle(DltMessageDTO dltMessage) {
        // Default strategy handles all messages as fallback
        return true;
    }

    @Override
    public int getPriority() {
        return 10; // Lowest priority - only used when no other strategy matches
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}