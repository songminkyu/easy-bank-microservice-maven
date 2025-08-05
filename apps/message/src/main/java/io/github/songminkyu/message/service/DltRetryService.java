package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltStrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

/**
 * Service for handling scheduled retries of DLT messages
 * Manages retry delays and ensures messages are reprocessed appropriately
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DltRetryService {

    private final TaskScheduler taskScheduler;
    private final StreamBridge streamBridge;
    private final Function<AccountsMsgDTO, AccountsMsgDTO> emailProcessor;
    private final Function<AccountsMsgDTO, Long> smsProcessor;
    private final DltStrategyManager dltStrategyManager;
    
    private final ConcurrentHashMap<String, ScheduledFuture<?>> scheduledRetries = new ConcurrentHashMap<>();

    /**
     * Schedule a message for retry based on processing result
     * 
     * @param dltMessage The message to retry
     * @param processingResult The result containing retry information
     */
    public void scheduleRetry(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        if (!processingResult.shouldRetry()) {
            log.warn("Retry not requested for message: account={}", 
                    dltMessage.originalMessage().accountNumber());
            return;
        }

        String retryKey = generateRetryKey(dltMessage);
        long delayMs = processingResult.retryDelayMs();

        log.info("Scheduling retry for account {} with delay {}ms (key: {})",
                dltMessage.originalMessage().accountNumber(),
                delayMs,
                retryKey);

        // Cancel any existing scheduled retry for this message
        cancelScheduledRetry(retryKey);

        // Schedule the retry
        Instant retryTime = Instant.now().plusMillis(delayMs);
        ScheduledFuture<?> future = taskScheduler.schedule(
            () -> executeRetry(dltMessage, retryKey),
            retryTime
        );

        scheduledRetries.put(retryKey, future);
        
        log.debug("Retry scheduled successfully: key={}, retryTime={}", retryKey, retryTime);
    }

    /**
     * Cancel a scheduled retry
     * 
     * @param dltMessage The message whose retry should be cancelled
     */
    public void cancelRetry(DltMessageDTO dltMessage) {
        String retryKey = generateRetryKey(dltMessage);
        cancelScheduledRetry(retryKey);
    }

    /**
     * Get the number of currently scheduled retries
     * 
     * @return Count of scheduled retries
     */
    public int getScheduledRetryCount() {
        return scheduledRetries.size();
    }

    /**
     * Check if a message has a scheduled retry
     * 
     * @param dltMessage The message to check
     * @return true if retry is scheduled
     */
    public boolean isRetryScheduled(DltMessageDTO dltMessage) {
        String retryKey = generateRetryKey(dltMessage);
        return scheduledRetries.containsKey(retryKey);
    }

    private String generateRetryKey(DltMessageDTO dltMessage) {
        // Create a unique key for this message retry
        return String.format("retry_%s_%s_%d",
                dltMessage.originalMessage().accountNumber(),
                dltMessage.exceptionClass(),
                dltMessage.failedAt().toEpochSecond(java.time.ZoneOffset.UTC));
    }

    private void cancelScheduledRetry(String retryKey) {
        ScheduledFuture<?> existingFuture = scheduledRetries.remove(retryKey);
        if (existingFuture != null && !existingFuture.isDone()) {
            existingFuture.cancel(false);
            log.debug("Cancelled existing scheduled retry: key={}", retryKey);
        }
    }

    private void executeRetry(DltMessageDTO dltMessage, String retryKey) {
        try {
            log.info("Executing scheduled retry for account {} (key: {})",
                    dltMessage.originalMessage().accountNumber(),
                    retryKey);

            // Remove from scheduled retries map
            scheduledRetries.remove(retryKey);

            AccountsMsgDTO originalMessage = dltMessage.originalMessage();
            boolean retrySuccessful = false;
            
            // Strategy 1: Direct processing retry
            try {
                log.debug("Attempting direct processing retry for account: {}", originalMessage.accountNumber());
                
                // Try processing with both email and SMS functions
                AccountsMsgDTO emailResult = emailProcessor.apply(originalMessage);
                Long smsResult = smsProcessor.apply(originalMessage);
                
                log.info("Direct retry successful for account {}: email={}, sms={}",
                        originalMessage.accountNumber(), emailResult != null, smsResult != null);
                retrySuccessful = true;
                
            } catch (Exception directRetryException) {
                log.warn("Direct processing retry failed for account {}: {}", 
                        originalMessage.accountNumber(), directRetryException.getMessage());
                
                // Strategy 2: Re-publish to original topic
                try {
                    log.debug("Attempting to re-publish message to original topic for account: {}", 
                            originalMessage.accountNumber());
                    
                    // Create message with retry headers
                    Message<AccountsMsgDTO> retryMessage = MessageBuilder
                            .withPayload(originalMessage)
                            .setHeader("x-retry-attempt", dltMessage.attemptCount() + 1)
                            .setHeader("x-retry-reason", "Scheduled retry from DLT")
                            .setHeader("x-original-failure", dltMessage.errorMessage())
                            .build();
                    
                    // Send to original topic (send-communication)
                    boolean sent = streamBridge.send("emailsms-out-0", retryMessage);
                    
                    if (sent) {
                        log.info("Message successfully re-published to original topic: account={}", 
                                originalMessage.accountNumber());
                        retrySuccessful = true;
                    } else {
                        log.error("Failed to re-publish message to original topic: account={}", 
                                originalMessage.accountNumber());
                    }
                    
                } catch (Exception republishException) {
                    log.error("Failed to re-publish message for account {}: {}", 
                            originalMessage.accountNumber(), republishException.getMessage());
                }
            }
            
            // Handle retry outcome
            if (retrySuccessful) {
                log.info("✅ Retry executed successfully for account {}", originalMessage.accountNumber());
                
                // Record successful retry metrics
                recordRetryMetrics(dltMessage, "success");
                
            } else {
                log.error("❌ All retry strategies failed for account {}", originalMessage.accountNumber());
                
                // Record failed retry metrics
                recordRetryMetrics(dltMessage, "failure");
                
                // Create updated DLT message with incremented attempt count
                DltMessageDTO updatedDltMessage = new DltMessageDTO(
                        originalMessage,
                        "Retry failed: " + dltMessage.errorMessage(),
                        dltMessage.exceptionClass(),
                        java.time.LocalDateTime.now(),
                        dltMessage.attemptCount() + 1,
                        "Scheduled retry failed - considering further action"
                );
                
                // Re-evaluate with strategy manager for potential escalation
                DltProcessingResult reprocessingResult = dltStrategyManager.processDltMessage(updatedDltMessage);
                
                if (reprocessingResult.shouldRetry() && reprocessingResult.retryDelayMs() > 0) {
                    log.info("Scheduling another retry for account {} with delay {}ms", 
                            originalMessage.accountNumber(), reprocessingResult.retryDelayMs());
                    scheduleRetry(updatedDltMessage, reprocessingResult);
                } else {
                    log.warn("No more retries scheduled for account {} - manual intervention may be required", 
                            originalMessage.accountNumber());
                }
            }
            
        } catch (Exception e) {
            log.error("Critical error during retry execution for account {} (key: {}): {}",
                    dltMessage.originalMessage().accountNumber(),
                    retryKey,
                    e.getMessage(), e);
                    
            // Record critical error metrics
            recordRetryMetrics(dltMessage, "critical_error");
            
            // Remove from retry map to prevent memory leaks
            scheduledRetries.remove(retryKey);
        }
    }
    
    /**
     * Record retry metrics for monitoring and analysis
     * 
     * @param dltMessage The message being retried
     * @param outcome The outcome of the retry attempt
     */
    private void recordRetryMetrics(DltMessageDTO dltMessage, String outcome) {
        // This could integrate with Micrometer metrics
        log.debug("Recording retry metrics: account={}, outcome={}, attemptCount={}",
                dltMessage.originalMessage().accountNumber(),
                outcome,
                dltMessage.attemptCount());
        
        // Future enhancement: Add actual metrics recording
        // meterRegistry.counter("dlt.retry.outcome", "result", outcome).increment();
    }
}