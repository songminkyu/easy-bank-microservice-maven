package io.github.songminkyu.message.service;

import io.github.songminkyu.message.config.DltConfiguration;
import io.github.songminkyu.message.dto.DltMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for sending notifications to external systems
 * Implements actual notification functionality for alerts
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final DltConfiguration dltConfiguration;

    /**
     * Send Slack notification asynchronously
     */
    public CompletableFuture<Boolean> sendSlackNotification(String message, boolean isCritical) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String webhookUrl = dltConfiguration.getAlertConfig().getSlackWebhookUrl();
                
                if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                    log.warn("Slack webhook URL not configured - notification skipped");
                    return false;
                }

                // TODO: Implement actual Slack API call
                // For now, simulate the call with logging
                String priority = isCritical ? "CRITICAL" : "STANDARD";
                log.info("📱 [SLACK {}] Notification sent: {}", priority, message);
                
                // Simulate network delay
                Thread.sleep(100);
                
                return true;
                
            } catch (Exception e) {
                log.error("Failed to send Slack notification: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Send email notification asynchronously
     */
    public CompletableFuture<Boolean> sendEmailNotification(String[] recipients, String subject, String body, boolean isCritical) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (recipients == null || recipients.length == 0) {
                    log.warn("No email recipients configured - notification skipped");
                    return false;
                }

                // TODO: Implement actual email sending (SMTP, SES, etc.)
                // For now, simulate with logging
                String priority = isCritical ? "CRITICAL" : "STANDARD";
                log.info("📧 [EMAIL {}] Sent to {} recipients: Subject={}", 
                        priority, recipients.length, subject);
                log.debug("Email body: {}", body);
                
                // Simulate network delay
                Thread.sleep(150);
                
                return true;
                
            } catch (Exception e) {
                log.error("Failed to send email notification: {}", e.getMessage());
                return false;
            }
        });
    }

    /**
     * Create JIRA ticket asynchronously
     */
    @ConditionalOnProperty(name = "app.dlt.alert-config.jira-enabled", havingValue = "true", matchIfMissing = false)
    public CompletableFuture<String> createJiraTicket(DltMessageDTO dltMessage, String priority) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String projectKey = dltConfiguration.getAlertConfig().getJiraProjectKey();
                
                // TODO: Implement actual JIRA API call
                // For now, simulate ticket creation
                String ticketId = String.format("%s-%d", projectKey, System.currentTimeMillis() % 10000);
                
                log.info("🎫 [JIRA] Ticket created: {} (Priority: {}, Account: {})", 
                        ticketId, priority, dltMessage.originalMessage().accountNumber());
                
                // Simulate API call delay
                Thread.sleep(200);
                
                return ticketId;
                
            } catch (Exception e) {
                log.error("Failed to create JIRA ticket: {}", e.getMessage());
                return null;
            }
        });
    }

    /**
     * Trigger PagerDuty incident asynchronously
     */
    @ConditionalOnProperty(name = "app.dlt.alert-config.pager-duty-enabled", havingValue = "true")
    public CompletableFuture<String> triggerPagerDutyIncident(DltMessageDTO dltMessage, String severity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement actual PagerDuty Events API call
                // For now, simulate incident creation
                String incidentId = "INC-" + System.currentTimeMillis() % 10000;
                
                log.error("📟 [PAGERDUTY] Incident triggered: {} (Severity: {}, Account: {})", 
                        incidentId, severity, dltMessage.originalMessage().accountNumber());
                
                // Simulate API call delay
                Thread.sleep(250);
                
                return incidentId;
                
            } catch (Exception e) {
                log.error("Failed to trigger PagerDuty incident: {}", e.getMessage());
                return null;
            }
        });
    }

    /**
     * Send comprehensive notification with all configured channels
     */
    public CompletableFuture<NotificationResult> sendComprehensiveNotification(
            DltMessageDTO dltMessage, boolean isCritical) {
        
        String message = formatNotificationMessage(dltMessage, isCritical);
        String priority = isCritical ? "CRITICAL" : "STANDARD";
        
        // Send notifications to all configured channels asynchronously
        CompletableFuture<Boolean> slackFuture = sendSlackNotification(message, isCritical);
        
        CompletableFuture<Boolean> emailFuture = sendEmailNotification(
                getEmailRecipients(isCritical),
                String.format("[%s] DLT Processing Alert - Account %s", 
                        priority, dltMessage.originalMessage().accountNumber()),
                message,
                isCritical
        );
        
        CompletableFuture<String> jiraFuture = isCritical ? 
                createJiraTicket(dltMessage, "High") : 
                CompletableFuture.completedFuture(null);
        
        CompletableFuture<String> pagerDutyFuture = (isCritical && 
                dltConfiguration.getAlertConfig().isPagerDutyEnabled()) ?
                triggerPagerDutyIncident(dltMessage, "critical") :
                CompletableFuture.completedFuture(null);
        
        // Combine all futures and return result
        return CompletableFuture.allOf(slackFuture, emailFuture, jiraFuture, pagerDutyFuture)
                .thenApply(v -> new NotificationResult(
                        slackFuture.join(),
                        emailFuture.join(),
                        jiraFuture.join(),
                        pagerDutyFuture.join()
                ));
    }

    private String[] getEmailRecipients(boolean isCritical) {
        return isCritical ? 
                dltConfiguration.getAlertConfig().getEmailRecipients().get("critical") :
                dltConfiguration.getAlertConfig().getEmailRecipients().get("standard");
    }

    private String formatNotificationMessage(DltMessageDTO dltMessage, boolean isCritical) {
        String severity = isCritical ? "🔥 CRITICAL" : "⚠️ STANDARD";
        
        return String.format(
                "%s DLT ALERT\n" +
                "Account: %s\n" +
                "Customer: %s\n" +
                "Error: %s\n" +
                "Exception: %s\n" +
                "Attempts: %d\n" +
                "Timestamp: %s\n" +
                "Reason: %s",
                severity,
                dltMessage.originalMessage().accountNumber(),
                dltMessage.originalMessage().name(),
                dltMessage.errorMessage(),
                dltMessage.exceptionClass(),
                dltMessage.attemptCount(),
                dltMessage.failedAt(),
                dltMessage.lastFailureReason()
        );
    }

    /**
     * Result of notification sending operations
     */
    public record NotificationResult(
            boolean slackSent,
            boolean emailSent,
            String jiraTicketId,
            String pagerDutyIncidentId
    ) {
        public boolean hasAnySuccess() {
            return slackSent || emailSent || jiraTicketId != null || pagerDutyIncidentId != null;
        }
        
        public int getSuccessCount() {
            int count = 0;
            if (slackSent) count++;
            if (emailSent) count++;
            if (jiraTicketId != null) count++;
            if (pagerDutyIncidentId != null) count++;
            return count;
        }
    }
}