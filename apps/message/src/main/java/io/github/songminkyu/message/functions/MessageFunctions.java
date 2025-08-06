package io.github.songminkyu.message.functions;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.event.RetryMessageEvent;
import io.github.songminkyu.message.event.RetryResultEvent;
import io.github.songminkyu.message.monitoring.DltMetrics;
import io.github.songminkyu.message.service.DltAlertService;
import io.github.songminkyu.message.service.MessageRetryService;
import io.github.songminkyu.message.strategy.AlertStrategyManager;
import io.github.songminkyu.message.strategy.DltProcessingResult;
import io.github.songminkyu.message.strategy.DltStrategyManager;
import io.micrometer.core.instrument.Timer;

import java.net.SocketTimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.cloud.function.context.FunctionCatalog;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.MessageHeaders;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MessageFunctions {

    private final DltMetrics dltMetrics;
    private final DltAlertService dltAlertService;
    private final DltStrategyManager dltStrategyManager;
    private final AlertStrategyManager alertStrategyManager;
    private final MessageRetryService messageRetryService;

    @PostConstruct
    public void init() {
        log.info("🚀 MessageFunctions initialized - email, sms, handleDltMessage beans will be created");
    }

    @Bean
    public Function<AccountsMsgDTO, AccountsMsgDTO> email() {
        log.info("🔧 Creating email() Function bean");
        return accountsMsgDto -> {
            try {
                log.info("★ Processing email for account: {} ★", accountsMsgDto.accountNumber());
                
                // Validate input data - these are permanent failures (no retry)
                if (accountsMsgDto.email() == null || accountsMsgDto.email().trim().isEmpty()) {
                    throw new IllegalArgumentException("Email address cannot be null or empty - permanent failure");
                }
                if (accountsMsgDto.accountNumber() == null) {
                    throw new IllegalArgumentException("Account number cannot be null for email processing - permanent failure");
                }
                
                // For testing: Force exception to test DLQ behavior
                // Comment out the next line in production
                // throw new IllegalArgumentException("TESTING: Forced exception to verify DLQ behavior");
                
                // Simulate email service call that might fail temporarily
                // if (someExternalServiceDown()) {
                //     throw new SocketTimeoutException("Email service temporarily unavailable - will retry");
                // }
                
                // Email processing logic here
                log.info("Email sent successfully to: {}", accountsMsgDto.email());
                return accountsMsgDto;

            } catch (IllegalArgumentException | NullPointerException e) {
                // Permanent failures - log and let Spring handle DLQ routing
                log.error("🔥 Permanent failure - this should trigger DLQ routing. Account: {}, Error: {}", 
                        accountsMsgDto.accountNumber(), e.getMessage());
                log.debug("📋 Exception details: Type={}, Message={}", e.getClass().getSimpleName(), e.getMessage());
                dltMetrics.recordMessageFailure("email", e.getClass().getSimpleName());
                throw e; // Rethrow as-is for DLQ routing
                
            } catch (Exception e) {
                // Unexpected exceptions - treat as permanent failure
                log.error("Unexpected error - sending to DLQ. Account: {}, Error: {}",
                        accountsMsgDto.accountNumber(), e.getMessage());
                dltMetrics.recordMessageFailure("email", e.getClass().getSimpleName());
                throw new IllegalArgumentException("Unexpected error in email processing: " + e.getMessage(), e);
            }
        };
    }

    @Bean
    public Function<AccountsMsgDTO, Long> sms() {
        log.info("🔧 Creating sms() Function bean");
        return accountsMsgDto -> {
            try {
                log.info("Sending sms with the details : " + accountsMsgDto.toString());

                // Simulate potential SMS service failure for demonstration
                if (accountsMsgDto.mobileNumber() == null || accountsMsgDto.mobileNumber().isEmpty()) {
                    throw new IllegalArgumentException("Mobile number cannot be null or empty for SMS processing");
                }

                // SMS processing logic here
                log.info("SMS sent successfully to: {}", accountsMsgDto.mobileNumber());
                return accountsMsgDto.accountNumber();

            } catch (Exception e) {
                log.error("Failed to send SMS for account: {}, error: {}",
                        accountsMsgDto.accountNumber(), e.getMessage());
                dltMetrics.recordMessageFailure("sms", e.getClass().getSimpleName());
                throw new RuntimeException("SMS processing failed: " + e.getMessage(), e);
            }
        };
    }

    @Bean
    public Consumer<Message<AccountsMsgDTO>> handleDltMessage() {
        log.info("🔧 handleDltMessage bean created successfully!");
        return message -> {
            log.error("🚨 DLT MESSAGE RECEIVED! Starting DLT processing...");
            log.debug("📋 DLT Message headers: {}", message.getHeaders());
            log.debug("📋 DLT Message payload type: {}", message.getPayload().getClass().getSimpleName());
            Timer.Sample sample = dltMetrics.startDltProcessingTimer();
            String outcome = "success";

            try {
                AccountsMsgDTO accountsMsgDto = message.getPayload();
                log.error("Processing message from DLT: Account Number: {}, Name: {}, Email: {}, Mobile: {}",
                        accountsMsgDto.accountNumber(),
                        accountsMsgDto.name(),
                        accountsMsgDto.email(),
                        accountsMsgDto.mobileNumber());

                // Extract error information from headers - handle byte arrays safely
                String errorMessage = getStringFromHeader(message.getHeaders(), "x-exception-message");
                String exceptionClass = getStringFromHeader(message.getHeaders(), "x-exception-fqcn");
                Integer attemptCount = getIntegerFromHeader(message.getHeaders(), "x-retry-count");

                // Create DLT message DTO for comprehensive logging
                DltMessageDTO dltMessage = new DltMessageDTO(
                        accountsMsgDto,
                        errorMessage,
                        exceptionClass,
                        java.time.LocalDateTime.now(),
                        attemptCount != null ? attemptCount : 0,
                        "Message processing failed after maximum retry attempts"
                );

                log.error("DLT Message processed: {}", dltMessage);

                // Record metrics
                dltMetrics.recordDltMessage(dltMessage);

                // Process DLT message using strategy pattern
                DltProcessingResult processingResult = dltStrategyManager.processDltMessage(dltMessage);
                
                // Send alerts based on processing result
                alertStrategyManager.sendAlert(dltMessage, processingResult);
                
                // Handle additional actions based on result
                handleProcessingResult(dltMessage, processingResult);

            } catch (Exception e) {
                outcome = "failure";
                log.error("Failed to process DLT message: {}", e.getMessage(), e);
                // Even DLT processing failed - this is critical
                // Should trigger immediate alert to operations team
            } finally {
                dltMetrics.recordDltProcessingTime(sample, outcome);
            }
        };
    }

    private void handleProcessingResult(DltMessageDTO dltMessage, DltProcessingResult processingResult) {
        log.info("DLT Processing completed for account {}: status={}, strategy={}, requiresIntervention={}",
                dltMessage.originalMessage().accountNumber(),
                processingResult.status(),
                processingResult.strategyUsed(),
                processingResult.requiresManualIntervention());

        // Handle specific actions based on processing result
        switch (processingResult.status()) {
            case RETRY_SCHEDULED -> {
                log.info("Message retry scheduled for account {} with delay {}ms",
                        dltMessage.originalMessage().accountNumber(),
                        processingResult.retryDelayMs());
                // Schedule the retry using the message retry service
                messageRetryService.scheduleRetry(dltMessage, processingResult);
            }
            case MANUAL_INTERVENTION_REQUIRED -> {
                log.warn("Manual intervention required for account {}: {}",
                        dltMessage.originalMessage().accountNumber(),
                        processingResult.message());
                // Additional tracking or workflow trigger could go here
            }
            case ESCALATED -> {
                log.error("DLT processing escalated for account {}: {}",
                        dltMessage.originalMessage().accountNumber(),
                        processingResult.message());
                // Emergency response procedures could be triggered here
            }
            case PERMANENT_FAILURE -> {
                log.error("Permanent failure recorded for account {}: {}",
                        dltMessage.originalMessage().accountNumber(),
                        processingResult.message());
                // Archive message to permanent failure store
            }
            case SUCCESS -> {
                log.info("DLT processing completed successfully for account {}",
                        dltMessage.originalMessage().accountNumber());
            }
        }
    }

    // Helper methods for safe header access
    private String getStringFromHeader(MessageHeaders headers, String headerName) {
        Object value = headers.get(headerName);
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof byte[]) {
            return new String((byte[]) value, StandardCharsets.UTF_8);
        }
        return value.toString();
    }

    private Integer getIntegerFromHeader(MessageHeaders headers, String headerName) {
        Object value = headers.get(headerName);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse header {} as integer: {}", headerName, value);
                return null;
            }
        }
        if (value instanceof byte[]) {
            try {
                String strValue = new String((byte[]) value, StandardCharsets.UTF_8);
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse header {} as integer from bytes: {}", headerName, value);
                return null;
            }
        }
        return null;
    }

}