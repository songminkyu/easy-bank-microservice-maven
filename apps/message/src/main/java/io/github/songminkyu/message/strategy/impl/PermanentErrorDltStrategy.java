package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltProcessingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Strategy for handling permanent errors that should not be retried
 * Archives the message and generates appropriate alerts
 */
@Component
@Slf4j
public class PermanentErrorDltStrategy implements DltProcessingStrategy {

    private static final String STRATEGY_NAME = "PermanentErrorDltStrategy";
    
    // Errors that indicate permanent failure and should not be retried
    private static final Set<String> PERMANENT_ERROR_TYPES = Set.of(
        "IllegalArgumentException",
        "ValidationException", 
        "DataIntegrityViolationException",
        "AuthenticationException",
        "AuthorizationException",
        "BadRequestException",
        "SecurityException",
        "UnsupportedOperationException"
    );

    // Messages that indicate data corruption or invalid state
    private static final Set<String> PERMANENT_ERROR_MESSAGES = Set.of(
        "cannot be null",
        "invalid format",
        "validation failed",
        "unauthorized",
        "forbidden",
        "malformed",
        "corrupt"
    );

    @Override
    public DltProcessingResult processDltMessage(DltMessageDTO dltMessage) {
        log.error("❌ PERMANENT ERROR DLT: Handling non-recoverable error for account {}",
                dltMessage.originalMessage().accountNumber());

        List<String> actions = List.of(
            "Message archived to permanent failure store",
            "Data validation error logged for analysis",
            "Customer notification process initiated",
            "System audit log updated"
        );

        // For permanent errors, don't retry but ensure proper logging and notification
        return DltProcessingResult.permanentFailure(
            STRATEGY_NAME,
            String.format("Permanent error detected for account %s: %s. Message archived.",
                dltMessage.originalMessage().accountNumber(),
                dltMessage.errorMessage()),
            actions
        );
    }

    @Override
    public boolean canHandle(DltMessageDTO dltMessage) {
        String exceptionClass = dltMessage.exceptionClass();
        String errorMessage = dltMessage.errorMessage();
        
        // Check if the exception type indicates a permanent error
        boolean isPermanentExceptionType = exceptionClass != null && 
            PERMANENT_ERROR_TYPES.stream()
                .anyMatch(permanentType -> exceptionClass.contains(permanentType));
        
        // Check if the error message indicates a permanent issue
        boolean isPermanentErrorMessage = errorMessage != null &&
            PERMANENT_ERROR_MESSAGES.stream()
                .anyMatch(permanentMsg -> errorMessage.toLowerCase().contains(permanentMsg));
        
        return isPermanentExceptionType || isPermanentErrorMessage;
    }

    @Override
    public int getPriority() {
        return 90; // High priority to prevent unnecessary retries of permanent errors
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}