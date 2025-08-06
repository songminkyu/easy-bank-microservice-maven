package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.service.NotificationService;
import io.github.songminkyu.message.strategy.AlertStrategy;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Alert strategy for critical situations requiring immediate attention
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CriticalAlertStrategy implements AlertStrategy {

    private final NotificationService notificationService;
    private static final String STRATEGY_NAME = "CriticalAlertStrategy";

    @Override
    public void sendAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult) {

        // Send immediate notifications
        sendPagerDutyAlert(dltMessage, processingResult);
        sendSlackUrgentNotification(dltMessage, processingResult);
        sendEmailToOnCallTeam(dltMessage, processingResult);

        // Create high-priority tickets
        createUrgentJiraTicket(dltMessage, processingResult);

        log.error("🚨 CRITICAL ALERT: Immediate attention required for account {}",
                dltMessage.originalMessage().accountNumber());

        // Send comprehensive notification via NotificationService
        notificationService.sendComprehensiveNotification(dltMessage, true)
                .thenAccept(result -> {
                    log.info("Critical notifications sent: {} channels successful", result.getSuccessCount());
                    
                    if (!result.hasAnySuccess()) {
                        log.error("❌ ALL CRITICAL NOTIFICATIONS FAILED for account {}", 
                                dltMessage.originalMessage().accountNumber());
                        
                        // Fallback: At minimum, log critical failure for manual monitoring
                        logCriticalFailureForManualMonitoring(dltMessage, processingResult);
                    } else {
                        // Log success details for audit trail
                        logNotificationSuccessDetails(dltMessage, result, processingResult);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Critical notification system failed for account {}: {}", 
                            dltMessage.originalMessage().accountNumber(), 
                            throwable.getMessage());
                    
                    // Fallback: Log for manual intervention
                    logCriticalFailureForManualMonitoring(dltMessage, processingResult);
                    return null;
                });
    }

    private void logCriticalFailureForManualMonitoring(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.error("🚨 MANUAL MONITORING REQUIRED: ALL NOTIFICATION CHANNELS FAILED");
        log.error("Account: {}, Customer: {}, Error: {}, Attempts: {}", 
                dltMessage.originalMessage().accountNumber(),
                dltMessage.originalMessage().name(),
                dltMessage.errorMessage(),
                dltMessage.attemptCount());
        log.error("Processing result: strategy={}, status={}, actions={}", 
                processingResult.strategyUsed(),
                processingResult.status(),
                processingResult.actionsTaken());
    }

    private void logNotificationSuccessDetails(DltMessageDTO dltMessage, 
                                             NotificationService.NotificationResult result, 
                                             DltProcessingResult processingResult) {
        log.info("✅ Critical alert successfully sent for account {}: Slack={}, Email={}, Jira={}, PagerDuty={}",
                dltMessage.originalMessage().accountNumber(),
                result.slackSent() ? "✓" : "✗",
                result.emailSent() ? "✓" : "✗", 
                result.jiraTicketId() != null ? result.jiraTicketId() : "✗",
                result.pagerDutyIncidentId() != null ? result.pagerDutyIncidentId() : "✗");
    }

    @Override
    public boolean shouldHandle(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        // Handle critical processing results or high-value account failures
        boolean isCriticalResult = processingResult.status() == DltProcessingResult.ProcessingStatus.ESCALATED ||
                                 processingResult.requiresManualIntervention();
        
        boolean isHighValueAccount = dltMessage.originalMessage().accountNumber() != null &&
                                   dltMessage.originalMessage().accountNumber() > 1000000000L;
        
        boolean isCriticalError = "NullPointerException".equals(dltMessage.exceptionClass()) ||
                                dltMessage.attemptCount() >= 5;
        
        return isCriticalResult || isHighValueAccount || isCriticalError;
    }

    @Override
    public int getPriority() {
        return 100; // Highest priority for critical alerts
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }


    private void sendPagerDutyAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        // Implement PagerDuty integration
        log.error("📟 PagerDuty alert triggered for account: {}, incident created",
                dltMessage.originalMessage().accountNumber());

        // PagerDuty API call would go here
        // {
        //   "incident_key": "dlt_critical_" + accountNumber,
        //   "event_type": "trigger",
        //   "description": "Critical DLT processing failure",
        //   "details": { ... }
        // }
    }

    private void sendSlackUrgentNotification(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.error("🚨 Urgent Slack notification sent for account: {}",
                dltMessage.originalMessage().accountNumber());

        // Slack webhook with @channel mention
        // {
        //   "channel": "#operations-urgent",
        //   "text": "@channel Critical DLT Alert",
        //   "attachments": [{
        //     "color": "danger",
        //     "urgency": "high"
        //   }]
        // }
    }

    private void sendEmailToOnCallTeam(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.error("📧 On-call team email sent for account: {}",
                dltMessage.originalMessage().accountNumber());

        // Email notification to on-call rotation
        // Subject: [CRITICAL] DLT Processing Failure - Immediate Action Required
        // Include: Full context, runbook links, escalation paths
    }

    private void createUrgentJiraTicket(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.error("🎫 Urgent Jira ticket created for account: {}",
                dltMessage.originalMessage().accountNumber());

        // Jira API call for P1 ticket
        // {
        //   "priority": "Highest",
        //   "labels": ["critical", "dlt", "production"],
        //   "assignee": "on-call-team"
        // }
    }

}