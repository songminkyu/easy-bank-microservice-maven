package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.dto.DltMessageDTO;

/**
 * Strategy interface for different alerting approaches based on DLT processing results
 */
public interface AlertStrategy {

    /**
     * Send appropriate alerts based on the DLT message and processing result
     * 
     * @param dltMessage The DLT message that was processed
     * @param processingResult The result of DLT processing
     */
    void sendAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult);

    /**
     * Determine if this alert strategy should handle the given scenario
     * 
     * @param dltMessage The DLT message
     * @param processingResult The processing result
     * @return true if this strategy should handle the alert
     */
    boolean shouldHandle(DltMessageDTO dltMessage, DltProcessingResult processingResult);

    /**
     * Get the priority of this alert strategy (higher number = higher priority)
     * 
     * @return priority value
     */
    int getPriority();

    /**
     * Get a descriptive name for this alert strategy
     * 
     * @return strategy name for logging and monitoring
     */
    String getStrategyName();
}