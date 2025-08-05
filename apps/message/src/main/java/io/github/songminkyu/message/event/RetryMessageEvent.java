package io.github.songminkyu.message.event;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event for triggering message retry processing
 * Published by DltRetryService and handled by MessageFunctions
 */
@Getter
public class RetryMessageEvent extends ApplicationEvent {
    
    private final AccountsMsgDTO originalMessage;
    private final String retryReason;
    private final int attemptCount;
    private final String retryKey;
    
    public RetryMessageEvent(Object source, AccountsMsgDTO originalMessage, String retryReason, 
                           int attemptCount, String retryKey) {
        super(source);
        this.originalMessage = originalMessage;
        this.retryReason = retryReason;
        this.attemptCount = attemptCount;
        this.retryKey = retryKey;
    }
}