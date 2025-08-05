package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.event.RetryMessageEvent;
import io.github.songminkyu.message.event.RetryResultEvent;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltStrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Service for handling retry message processing
 * Decouples MessageFunctions from DltRetryService to avoid circular dependency
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageRetryService {

    private final ApplicationContext applicationContext;
    private final ApplicationEventPublisher eventPublisher;
    private final DltStrategyManager dltStrategyManager;

    /**
     * Handle retry message events from DltRetryService
     */
    @EventListener
    public void handleRetryMessage(RetryMessageEvent event) {
        log.info("Received retry event for account: {}", event.getOriginalMessage().accountNumber());
        
        AccountsMsgDTO originalMessage = event.getOriginalMessage();
        String retryKey = event.getRetryKey();
        
        try {
            // Lazy initialization to avoid circular dependency
            Function<AccountsMsgDTO, AccountsMsgDTO> emailProcessor = 
                applicationContext.getBean("email", Function.class);
            Function<AccountsMsgDTO, Long> smsProcessor = 
                applicationContext.getBean("sms", Function.class);
            
            // Try processing with both email and SMS functions
            AccountsMsgDTO emailResult = emailProcessor.apply(originalMessage);
            Long smsResult = smsProcessor.apply(originalMessage);
            
            log.info("Retry processing successful for account {}: email={}, sms={}",
                    originalMessage.accountNumber(), emailResult != null, smsResult != null);
                    
            // Publish success event
            RetryResultEvent resultEvent = RetryResultEvent.success(this, originalMessage, retryKey);
            eventPublisher.publishEvent(resultEvent);
            
        } catch (Exception e) {
            log.error("Retry processing failed for account {}: {}", 
                    originalMessage.accountNumber(), e.getMessage());
                    
            // Publish failure event
            RetryResultEvent resultEvent = RetryResultEvent.failure(
                this, originalMessage, retryKey, e.getMessage(), e);
            eventPublisher.publishEvent(resultEvent);
        }
    }

    /**
     * Schedule retry using DLT strategy manager
     */
    public void scheduleRetry(io.github.songminkyu.message.dto.DltMessageDTO dltMessage, 
                             DltProcessingResult processingResult) {
        log.info("Scheduling retry for account {} with delay {}ms",
                dltMessage.originalMessage().accountNumber(),
                processingResult.retryDelayMs());
        
        // This method is called by MessageFunctions to maintain the interface
        // The actual retry logic is handled by DltRetryService
        // This service acts as a bridge to avoid circular dependency
    }
}