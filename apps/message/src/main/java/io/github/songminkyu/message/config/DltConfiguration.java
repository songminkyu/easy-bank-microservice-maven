package io.github.songminkyu.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Configuration properties for DLT processing strategies
 * Allows fine-tuning of DLT behavior through application properties
 */
@Configuration
@ConfigurationProperties(prefix = "app.dlt")
@Data
public class DltConfiguration {

    /**
     * Critical account configuration
     */
    private CriticalAccount criticalAccount = new CriticalAccount();

    /**
     * Retry strategy configuration
     */
    private RetryStrategy retryStrategy = new RetryStrategy();

    /**
     * Alert configuration
     */
    private AlertConfig alertConfig = new AlertConfig();

    /**
     * Permanent error configuration
     */
    private PermanentError permanentError = new PermanentError();

    @Data
    public static class CriticalAccount {
        /**
         * Account number threshold for critical account classification
         */
        private Long accountThreshold = 1000000000L;

        /**
         * Whether to immediately escalate critical account failures
         */
        private boolean immediateEscalation = true;

        /**
         * Whether to require manual intervention for critical accounts
         */
        private boolean requireManualIntervention = true;
    }

    @Data
    public static class RetryStrategy {
        /**
         * Maximum retry attempts from DLT
         */
        private int maxDltRetryAttempts = 2;

        /**
         * Base retry delay in milliseconds
         */
        private long baseRetryDelayMs = 30000L;

        /**
         * Exponential backoff multiplier
         */
        private double backoffMultiplier = 2.0;

        /**
         * Maximum retry delay in milliseconds
         */
        private long maxRetryDelayMs = 300000L; // 5 minutes

        /**
         * Transient error types that should be retried
         */
        private String[] transientErrorTypes = {
            "ConnectException",
            "SocketTimeoutException", 
            "TimeoutException",
            "ServiceUnavailableException",
            "TemporaryFailureException",
            "CircuitBreakerOpenException"
        };
    }

    @Data
    public static class AlertConfig {
        /**
         * Alert attempt count threshold for escalation
         */
        private int escalationAttemptThreshold = 5;

        /**
         * Whether to enable PagerDuty integration
         */
        private boolean pagerDutyEnabled = false;

        /**
         * Slack webhook URL for notifications
         */
        private String slackWebhookUrl;

        /**
         * Email recipients for different alert levels
         */
        private Map<String, String[]> emailRecipients = Map.of(
            "critical", new String[]{"ops-team@company.com", "on-call@company.com"},
            "standard", new String[]{"dev-team@company.com"}
        );

        /**
         * Jira project key for ticket creation
         */
        private String jiraProjectKey = "OPS";
    }

    @Data
    public static class PermanentError {
        /**
         * Error types that should be treated as permanent failures
         */
        private String[] permanentErrorTypes = {
            "IllegalArgumentException",
            "ValidationException", 
            "DataIntegrityViolationException",
            "AuthenticationException",
            "AuthorizationException",
            "BadRequestException",
            "SecurityException",
            "UnsupportedOperationException"
        };

        /**
         * Error message patterns that indicate permanent failures
         */
        private String[] permanentErrorMessages = {
            "cannot be null",
            "invalid format",
            "validation failed",
            "unauthorized",
            "forbidden",
            "malformed",
            "corrupt"
        };

        /**
         * Whether to archive permanent failure messages
         */
        private boolean archiveFailures = true;
    }
}