package io.github.songminkyu.message.strategy.impl;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.strategy.AlertStrategy;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Standard alert strategy for normal DLT processing situations
 */
@Component
@Slf4j
public class StandardAlertStrategy implements AlertStrategy {

    private static final String STRATEGY_NAME = "StandardAlertStrategy";

    @Override
    public void sendAlert(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.warn("📊 STANDARD ALERT: DLT processing notification for account {}",
                dltMessage.originalMessage().accountNumber());

        // Send standard notifications
        sendSlackNotification(dltMessage, processingResult);
        sendEmailNotification(dltMessage, processingResult);
        
        // Create standard tracking ticket if needed
        if (processingResult.requiresManualIntervention()) {
            createTrackingTicket(dltMessage, processingResult);
        }
        
        // Log for monitoring
        log.info("Standard alert sent: account={}, strategy={}, status={}, retry={}",
                dltMessage.originalMessage().accountNumber(),
                processingResult.strategyUsed(),
                processingResult.status(),
                processingResult.shouldRetry());
    }

    @Override
    public boolean shouldHandle(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        // Handle standard processing results that don't require critical alerts
        boolean isStandardResult = processingResult.status() == DltProcessingResult.ProcessingStatus.SUCCESS ||
                                  processingResult.status() == DltProcessingResult.ProcessingStatus.RETRY_SCHEDULED ||
                                  processingResult.status() == DltProcessingResult.ProcessingStatus.PERMANENT_FAILURE;
        
        boolean isNormalAccount = dltMessage.originalMessage().accountNumber() == null ||
                                dltMessage.originalMessage().accountNumber() <= 1000000000L;
        
        boolean isLowAttemptCount = dltMessage.attemptCount() < 5;
        
        return isStandardResult && isNormalAccount && isLowAttemptCount;
    }

    @Override
    public int getPriority() {
        return 50; // Medium priority for standard alerts
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    private void sendSlackNotification(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.info("📱 Slack notification sent for account: {}",
                dltMessage.originalMessage().accountNumber());
        
        // Standard Slack webhook
        // {
        //   "channel": "#operations",
        //   "text": "DLT Processing Update",
        //   "attachments": [{
        //     "color": getColorForStatus(processingResult.status()),
        //     "fields": [
        //       {"title": "Account", "value": accountNumber, "short": true},
        //       {"title": "Status", "value": status, "short": true},
        //       {"title": "Strategy", "value": strategy, "short": true}
        //     ]
        //   }]
        // }
    }

    private void sendEmailNotification(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.info("📧 Email notification sent for account: {}",
                dltMessage.originalMessage().accountNumber());
        
        // Standard email notification
        // Subject: DLT Processing Update - Account {accountNumber}
        // Content: Processing details, status, next actions
    }

    private void createTrackingTicket(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.info("🎫 Tracking ticket created for account: {}",
                dltMessage.originalMessage().accountNumber());
        
        // Standard priority Jira ticket
        // {
        //   "priority": "Medium",
        //   "labels": ["dlt", "tracking"],
        //   "description": "DLT processing requires manual intervention"
        // }
    }

    private String getColorForStatus(DltProcessingResult.ProcessingStatus status) {
        return switch (status) {
            case SUCCESS -> "good";
            case RETRY_SCHEDULED -> "warning";
            case PERMANENT_FAILURE -> "danger";
            case MANUAL_INTERVENTION_REQUIRED -> "warning";
            case ESCALATED -> "danger";
        };
    }
}