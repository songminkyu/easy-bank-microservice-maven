package io.github.songminkyu.message.functions;

import io.github.songminkyu.message.dto.AccountsMsgDTO;
import io.github.songminkyu.message.dto.DltMessageDTO;
import io.github.songminkyu.message.monitoring.DltMetrics;
import io.github.songminkyu.message.service.DltAlertService;
import io.micrometer.core.instrument.Timer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MessageFunctions {

    private final DltMetrics dltMetrics;
    private final DltAlertService dltAlertService;

    @Bean
    public Function<AccountsMsgDTO, AccountsMsgDTO> email() {
        return accountsMsgDto -> {
            try {
                log.info("★ Sending email with the details ★ : " + accountsMsgDto.toString());

                // Simulate potential email service failure for demonstration
                if (accountsMsgDto.accountNumber() == null) {
                    throw new IllegalArgumentException("Account number cannot be null for email processing");
                }

                // Email processing logic here
                log.info("Email sent successfully to: {}", accountsMsgDto.email());
                return accountsMsgDto;

            } catch (Exception e) {
                log.error("Failed to send email for account: {}, error: {}",
                        accountsMsgDto.accountNumber(), e.getMessage());
                dltMetrics.recordMessageFailure("email", e.getClass().getSimpleName());
                throw new RuntimeException("Email processing failed: " + e.getMessage(), e);
            }
        };
    }

    @Bean
    public Function<AccountsMsgDTO, Long> sms() {
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
        return message -> {
            Timer.Sample sample = dltMetrics.startDltProcessingTimer();
            String outcome = "success";

            try {
                AccountsMsgDTO accountsMsgDto = message.getPayload();
                log.error("Processing message from DLT: Account Number: {}, Name: {}, Email: {}, Mobile: {}",
                        accountsMsgDto.accountNumber(),
                        accountsMsgDto.name(),
                        accountsMsgDto.email(),
                        accountsMsgDto.mobileNumber());

                // Extract error information from headers
                String errorMessage = (String) message.getHeaders().get("x-exception-message");
                String exceptionClass = (String) message.getHeaders().get("x-exception-fqcn");
                Integer attemptCount = (Integer) message.getHeaders().get("x-retry-count");

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

                // Handle DLT notification and alerting
                handleDltNotification(dltMessage);

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

    private void handleDltNotification(DltMessageDTO dltMessage) {
        log.warn("DLT Notification: Failed message for account {} after {} attempts. Error: {}",
                dltMessage.originalMessage().accountNumber(),
                dltMessage.attemptCount(),
                dltMessage.errorMessage());

        // Send appropriate alerts based on failure severity
        if (dltAlertService.isCriticalFailure(dltMessage)) {
            dltAlertService.sendCriticalAlert(dltMessage);
        } else {
            dltAlertService.sendDltAlert(dltMessage);
        }
    }

}