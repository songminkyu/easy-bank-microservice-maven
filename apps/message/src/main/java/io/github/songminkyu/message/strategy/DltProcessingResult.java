package io.github.songminkyu.message.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of DLT message processing, containing outcome and actions taken
 */
public record DltProcessingResult(
    ProcessingStatus status,
    String message,
    LocalDateTime processedAt,
    List<String> actionsTaken,
    boolean requiresManualIntervention,
    boolean shouldRetry,
    long retryDelayMs,
    String strategyUsed
) {

    public enum ProcessingStatus {
        SUCCESS("Message processed successfully"),
        RETRY_SCHEDULED("Message scheduled for retry"),
        MANUAL_INTERVENTION_REQUIRED("Manual intervention required"),
        PERMANENT_FAILURE("Permanent failure - message discarded"),
        ESCALATED("Escalated to operations team");

        private final String description;

        ProcessingStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Create a successful processing result
     */
    public static DltProcessingResult success(String strategyName, String message) {
        return new DltProcessingResult(
            ProcessingStatus.SUCCESS,
            message,
            LocalDateTime.now(),
            new ArrayList<>(),
            false,
            false,
            0L,
            strategyName
        );
    }

    /**
     * Create a retry result with delay
     */
    public static DltProcessingResult retry(String strategyName, String message, long delayMs) {
        return new DltProcessingResult(
            ProcessingStatus.RETRY_SCHEDULED,
            message,
            LocalDateTime.now(),
            List.of("Scheduled for retry"),
            false,
            true,
            delayMs,
            strategyName
        );
    }

    /**
     * Create a manual intervention required result
     */
    public static DltProcessingResult manualIntervention(String strategyName, String message, List<String> actions) {
        return new DltProcessingResult(
            ProcessingStatus.MANUAL_INTERVENTION_REQUIRED,
            message,
            LocalDateTime.now(),
            actions != null ? new ArrayList<>(actions) : new ArrayList<>(),
            true,
            false,
            0L,
            strategyName
        );
    }

    /**
     * Create a permanent failure result
     */
    public static DltProcessingResult permanentFailure(String strategyName, String message, List<String> actions) {
        return new DltProcessingResult(
            ProcessingStatus.PERMANENT_FAILURE,
            message,
            LocalDateTime.now(),
            actions != null ? new ArrayList<>(actions) : new ArrayList<>(),
            false,
            false,
            0L,
            strategyName
        );
    }

    /**
     * Create an escalated result
     */
    public static DltProcessingResult escalated(String strategyName, String message, List<String> actions) {
        return new DltProcessingResult(
            ProcessingStatus.ESCALATED,
            message,
            LocalDateTime.now(),
            actions != null ? new ArrayList<>(actions) : new ArrayList<>(),
            true,
            false,
            0L,
            strategyName
        );
    }

    /**
     * Add an action to the result
     */
    public DltProcessingResult withAction(String action) {
        List<String> newActions = new ArrayList<>(this.actionsTaken);
        newActions.add(action);
        return new DltProcessingResult(
            this.status,
            this.message,
            this.processedAt,
            newActions,
            this.requiresManualIntervention,
            this.shouldRetry,
            this.retryDelayMs,
            this.strategyUsed
        );
    }
}