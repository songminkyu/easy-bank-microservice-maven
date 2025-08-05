package io.github.songminkyu.message.dto;

import java.time.LocalDateTime;

/**
 * DTO for Dead Letter Topic message handling
 */
public record DltMessageDTO(
    AccountsMsgDTO originalMessage,
    String errorMessage,
    String exceptionClass,
    LocalDateTime failedAt,
    int attemptCount,
    String lastFailureReason
) {
    
    public static DltMessageDTO from(AccountsMsgDTO originalMessage, Exception exception, int attemptCount) {
        return new DltMessageDTO(
            originalMessage,
            exception.getMessage(),
            exception.getClass().getSimpleName(),
            LocalDateTime.now(),
            attemptCount,
            exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()
        );
    }
}