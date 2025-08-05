package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltProcessingStrategy;
import io.github.songminkyu.message.config.DltConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Strategy for handling transient errors that might succeed on retry
 * Applies exponential backoff and limited retry attempts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransientErrorDltStrategy implements DltProcessingStrategy {

    private static final String STRATEGY_NAME = "TransientErrorDltStrategy";
    
    private final DltConfiguration dltConfiguration;

    @Override
    public DltProcessingResult processDltMessage(DltMessageDTO dltMessage) {
        log.warn("⏰ TRANSIENT ERROR DLT: Handling potentially recoverable error for account {}",
                dltMessage.originalMessage().accountNumber());

        // Check if we've already retried this message too many times from DLT
        int currentAttempts = dltMessage.attemptCount();
        
        if (currentAttempts < dltConfiguration.getRetryStrategy().getMaxDltRetryAttempts()) {
            // Calculate exponential backoff delay
            long retryDelay = calculateRetryDelay(currentAttempts);
            
            log.info("Scheduling retry attempt {} for account {} with delay {}ms",
                    currentAttempts + 1, 
                    dltMessage.originalMessage().accountNumber(),
                    retryDelay);

            return DltProcessingResult.retry(
                STRATEGY_NAME,
                String.format("Transient error detected, scheduling retry attempt %d for account %s",
                    currentAttempts + 1,
                    dltMessage.originalMessage().accountNumber()),
                retryDelay
            ).withAction("Exponential backoff applied")
             .withAction("Retry scheduled");
        } else {
            // Exceeded retry attempts, escalate
            log.warn("Maximum DLT retry attempts ({}) exceeded for account {}, escalating",
                    dltConfiguration.getRetryStrategy().getMaxDltRetryAttempts(),
                    dltMessage.originalMessage().accountNumber());

            return DltProcessingResult.manualIntervention(
                STRATEGY_NAME,
                String.format("Transient error persists after %d retry attempts for account %s",
                    dltConfiguration.getRetryStrategy().getMaxDltRetryAttempts(),
                    dltMessage.originalMessage().accountNumber()),
                List.of(
                    "Maximum retry attempts exceeded",
                    "Service health check recommended",
                    "Manual investigation required"
                )
            );
        }
    }

    @Override
    public boolean canHandle(DltMessageDTO dltMessage) {
        String exceptionClass = dltMessage.exceptionClass();
        if (exceptionClass == null) {
            return false;
        }
        
        String[] transientErrorTypes = dltConfiguration.getRetryStrategy().getTransientErrorTypes();
        return java.util.Arrays.stream(transientErrorTypes)
            .anyMatch(transientType -> exceptionClass.contains(transientType));
    }

    @Override
    public int getPriority() {
        return 80; // High priority but lower than critical account strategy
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    private long calculateRetryDelay(int attemptCount) {
        // Exponential backoff with configurable parameters
        DltConfiguration.RetryStrategy config = dltConfiguration.getRetryStrategy();
        long delay = (long) (config.getBaseRetryDelayMs() * Math.pow(config.getBackoffMultiplier(), attemptCount));
        return Math.min(delay, config.getMaxRetryDelayMs());
    }
}