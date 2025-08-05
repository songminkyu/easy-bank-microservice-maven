package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.dto.DltMessageDTO;

/**
 * Strategy interface for different DLT processing approaches
 * Enables pluggable strategies based on error type, message type, or business rules
 */
public interface DltProcessingStrategy {

    /**
     * Process a DLT message with specific strategy logic
     * 
     * @param dltMessage The DLT message to process
     * @return DltProcessingResult containing outcome and actions taken
     */
    DltProcessingResult processDltMessage(DltMessageDTO dltMessage);

    /**
     * Determine if this strategy can handle the given DLT message
     * 
     * @param dltMessage The DLT message to evaluate
     * @return true if this strategy should handle the message
     */
    boolean canHandle(DltMessageDTO dltMessage);

    /**
     * Get the priority of this strategy (higher number = higher priority)
     * Used when multiple strategies can handle the same message
     * 
     * @return priority value
     */
    int getPriority();

    /**
     * Get a descriptive name for this strategy
     * 
     * @return strategy name for logging and monitoring
     */
    String getStrategyName();
}