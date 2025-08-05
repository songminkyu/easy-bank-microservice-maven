package io.github.songminkyu.message.service;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Service for handling scheduled retries of DLT messages
 * Manages retry delays and ensures messages are reprocessed appropriately
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DltRetryService {

    private final TaskScheduler taskScheduler;
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

            // TODO: Implement actual retry mechanism
            // This would typically involve:
            // 1. Re-publishing the message to the original topic
            // 2. Or calling the original processing function directly
            // 3. Or adding to a retry queue
            
            // For now, just log the retry attempt
            log.info("Retry executed for account {}: {}", 
                    dltMessage.originalMessage().accountNumber(),
                    dltMessage.originalMessage());

            // In a real implementation, you might:
            // messageProducer.send(originalTopic, dltMessage.originalMessage());
            // or
            // messageProcessor.processMessage(dltMessage.originalMessage());
            
        } catch (Exception e) {
            log.error("Failed to execute retry for account {} (key: {}): {}",
                    dltMessage.originalMessage().accountNumber(),
                    retryKey,
                    e.getMessage(), e);
                    
            // Could implement exponential backoff here or escalate the failure
        }
    }
}