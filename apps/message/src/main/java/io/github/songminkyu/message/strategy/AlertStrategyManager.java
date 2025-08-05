package io.github.songminkyu.message.strategy;

import io.github.songminkyu.message.dto.DltMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Manager for coordinating alert strategies based on DLT processing results
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertStrategyManager {

    private final List<AlertStrategy> alertStrategies;

    /**
     * Send appropriate alerts based on DLT message and processing result
     * Selects the highest priority strategy that can handle the scenario
     * 
     * @param dltMessage The DLT message that was processed
     * @param processingResult The result of DLT processing
     */
    public void sendAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.debug("Selecting alert strategy for account {} with status {}",
                dltMessage.originalMessage().accountNumber(),
                processingResult.status());

        // Find the best alert strategy for this scenario
        List<AlertStrategy> applicableStrategies = alertStrategies.stream()
                .filter(strategy -> strategy.shouldHandle(dltMessage, processingResult))
                .sorted(Comparator.comparingInt(AlertStrategy::getPriority).reversed())
                .toList();

        if (applicableStrategies.isEmpty()) {
            log.warn("No suitable alert strategy found for DLT message: account={}, status={}",
                    dltMessage.originalMessage().accountNumber(),
                    processingResult.status());
            return;
        }

        // Use the highest priority strategy that can handle this scenario
        AlertStrategy selectedStrategy = applicableStrategies.get(0);
        
        log.info("Selected alert strategy '{}' for account {} with status {}",
                selectedStrategy.getStrategyName(),
                dltMessage.originalMessage().accountNumber(),
                processingResult.status());

        try {
            selectedStrategy.sendAlert(dltMessage, processingResult);
            
            log.info("Alert sent successfully: strategy={}, account={}, status={}",
                    selectedStrategy.getStrategyName(),
                    dltMessage.originalMessage().accountNumber(),
                    processingResult.status());
                    
        } catch (Exception e) {
            log.error("Alert strategy '{}' failed for account {}: {}",
                    selectedStrategy.getStrategyName(),
                    dltMessage.originalMessage().accountNumber(),
                    e.getMessage(), e);
            
            // Try fallback alert strategies
            sendFallbackAlert(dltMessage, processingResult, applicableStrategies, 1);
        }
    }

    /**
     * Attempt fallback alert strategies if the primary strategy fails
     */
    private void sendFallbackAlert(DltMessageDTO dltMessage, 
                                  DltProcessingResult processingResult,
                                  List<AlertStrategy> strategies, 
                                  int attemptIndex) {
        if (attemptIndex >= strategies.size()) {
            log.error("All alert strategies failed for account {}",
                    dltMessage.originalMessage().accountNumber());
            return;
        }

        AlertStrategy fallbackStrategy = strategies.get(attemptIndex);
        log.warn("Attempting fallback alert strategy '{}' for account {}",
                fallbackStrategy.getStrategyName(),
                dltMessage.originalMessage().accountNumber());

        try {
            fallbackStrategy.sendAlert(dltMessage, processingResult);
            log.info("Fallback alert successful: strategy={}, account={}",
                    fallbackStrategy.getStrategyName(),
                    dltMessage.originalMessage().accountNumber());
        } catch (Exception e) {
            log.error("Fallback alert strategy '{}' also failed for account {}: {}",
                    fallbackStrategy.getStrategyName(),
                    dltMessage.originalMessage().accountNumber(),
                    e.getMessage(), e);
            
            // Try next fallback
            sendFallbackAlert(dltMessage, processingResult, strategies, attemptIndex + 1);
        }
    }

    /**
     * Get information about all available alert strategies
     * 
     * @return List of alert strategy information
     */
    public List<AlertStrategyInfo> getAvailableAlertStrategies() {
        return alertStrategies.stream()
                .sorted(Comparator.comparingInt(AlertStrategy::getPriority).reversed())
                .map(strategy -> new AlertStrategyInfo(
                    strategy.getStrategyName(),
                    strategy.getPriority(),
                    strategy.getClass().getSimpleName()
                ))
                .toList();
    }

    /**
     * Information about an alert strategy
     */
    public record AlertStrategyInfo(
        String name,
        int priority,
        String className
    ) {}
}