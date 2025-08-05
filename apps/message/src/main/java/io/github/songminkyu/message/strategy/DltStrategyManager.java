package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.dto.DltMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Manager for coordinating DLT processing strategies
 * Selects the most appropriate strategy based on message characteristics and priority
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DltStrategyManager {

    private final List<DltProcessingStrategy> strategies;

    /**
     * Process a DLT message by selecting and executing the most appropriate strategy
     * 
     * @param dltMessage The DLT message to process
     * @return Result of the processing operation
     */
    public DltProcessingResult processDltMessage(DltMessageDTO dltMessage) {
        log.debug("Processing DLT message for account {} with {} available strategies",
                dltMessage.originalMessage().accountNumber(), strategies.size());

        // Find the best strategy for this message
        Optional<DltProcessingStrategy> selectedStrategy = selectStrategy(dltMessage);

        if (selectedStrategy.isEmpty()) {
            log.error("No suitable strategy found for DLT message: account={}, error={}",
                    dltMessage.originalMessage().accountNumber(),
                    dltMessage.errorMessage());
            
            return DltProcessingResult.permanentFailure(
                "NoStrategyFound",
                "No suitable DLT processing strategy could be found for this message",
                List.of("Message logged for manual analysis", "System configuration review recommended")
            );
        }

        DltProcessingStrategy strategy = selectedStrategy.get();
        log.info("Selected strategy '{}' for DLT message: account={}, priority={}",
                strategy.getStrategyName(),
                dltMessage.originalMessage().accountNumber(),
                strategy.getPriority());

        try {
            // Execute the selected strategy
            DltProcessingResult result = strategy.processDltMessage(dltMessage);
            
            log.info("DLT processing completed: strategy={}, status={}, account={}, requiresIntervention={}",
                    result.strategyUsed(),
                    result.status(),
                    dltMessage.originalMessage().accountNumber(),
                    result.requiresManualIntervention());

            return result;
            
        } catch (Exception e) {
            log.error("Strategy '{}' failed to process DLT message for account {}: {}",
                    strategy.getStrategyName(),
                    dltMessage.originalMessage().accountNumber(),
                    e.getMessage(), e);

            return DltProcessingResult.escalated(
                strategy.getStrategyName() + "_ERROR",
                String.format("Strategy execution failed: %s", e.getMessage()),
                List.of(
                    "Strategy execution error logged",
                    "Immediate technical review required",
                    "Fallback processing recommended"
                )
            );
        }
    }

    /**
     * Select the most appropriate strategy for the given DLT message
     * Strategies are evaluated by priority (highest first) and capability to handle the message
     * 
     * @param dltMessage The DLT message to find a strategy for
     * @return The selected strategy, or empty if none found
     */
    private Optional<DltProcessingStrategy> selectStrategy(DltMessageDTO dltMessage) {
        return strategies.stream()
                .filter(strategy -> strategy.canHandle(dltMessage))
                .max(Comparator.comparingInt(DltProcessingStrategy::getPriority));
    }

    /**
     * Get information about all available strategies
     * Useful for monitoring and debugging
     * 
     * @return List of strategy information
     */
    public List<StrategyInfo> getAvailableStrategies() {
        return strategies.stream()
                .sorted(Comparator.comparingInt(DltProcessingStrategy::getPriority).reversed())
                .map(strategy -> new StrategyInfo(
                    strategy.getStrategyName(),
                    strategy.getPriority(),
                    strategy.getClass().getSimpleName()
                ))
                .toList();
    }

    /**
     * Information about a DLT processing strategy
     */
    public record StrategyInfo(
        String name,
        int priority,
        String className
    ) {}
}