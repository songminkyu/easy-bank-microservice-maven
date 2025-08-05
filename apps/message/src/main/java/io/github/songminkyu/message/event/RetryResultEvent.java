package io.github.songminkyu.message.event;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event for reporting retry processing results
 * Published by MessageFunctions and handled by DltRetryService
 */
@Getter
public class RetryResultEvent extends ApplicationEvent {
    
    private final AccountsMsgDTO originalMessage;
    private final String retryKey;
    private final boolean successful;
    private final String errorMessage;
    private final Exception exception;
    
    public RetryResultEvent(Object source, AccountsMsgDTO originalMessage, String retryKey, 
                          boolean successful, String errorMessage, Exception exception) {
        super(source);
        this.originalMessage = originalMessage;
        this.retryKey = retryKey;
        this.successful = successful;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }
    
    public static RetryResultEvent success(Object source, AccountsMsgDTO originalMessage, String retryKey) {
        return new RetryResultEvent(source, originalMessage, retryKey, true, null, null);
    }
    
    public static RetryResultEvent failure(Object source, AccountsMsgDTO originalMessage, String retryKey, 
                                         String errorMessage, Exception exception) {
        return new RetryResultEvent(source, originalMessage, retryKey, false, errorMessage, exception);
    }
}