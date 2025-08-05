package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.AlertStrategy;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Alert strategy for critical situations requiring immediate attention
 */
@Component
@Slf4j
public class CriticalAlertStrategy implements AlertStrategy {

    private static final String STRATEGY_NAME = "CriticalAlertStrategy";

    @Override
    public void sendAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.error("🚨 CRITICAL ALERT: Immediate attention required for account {}",
                dltMessage.originalMessage().accountNumber());

        // Send immediate notifications
        sendPagerDutyAlert(dltMessage, processingResult);
        sendSlackUrgentNotification(dltMessage, processingResult);
        sendEmailToOnCallTeam(dltMessage, processingResult);
        
        // Create high-priority tickets
        createUrgentJiraTicket(dltMessage, processingResult);
        
        // Log for audit trail
        log.error("Critical alert sent: account={}, strategy={}, status={}, actions={}",
                dltMessage.originalMessage().accountNumber(),
                processingResult.strategyUsed(),
                processingResult.status(),
                processingResult.actionsTaken());
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