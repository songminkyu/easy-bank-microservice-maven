package io.github.songminkyu.message.monitoring;

import io.github.songminkyu.message.dto.DltMessageDTO;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Metrics collection for Dead Letter Topic monitoring
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DltMetrics {

    private final MeterRegistry meterRegistry;

    private static final String DLT_COUNTER_NAME = "message.dlt.received";
    private static final String DLT_PROCESSING_TIME = "message.dlt.processing.time";
    private static final String MESSAGE_FAILURE_COUNTER = "message.processing.failures";

    /**
     * Record DLT message reception
     */
    public void recordDltMessage(DltMessageDTO dltMessage) {
        Counter.builder(DLT_COUNTER_NAME)
                .tag("exception.class", dltMessage.exceptionClass())
                .tag("account.number", String.valueOf(dltMessage.originalMessage().accountNumber()))
                .description("Number of messages sent to DLT")
                .register(meterRegistry)
                .increment();

        log.debug("DLT metric recorded for account: {}", dltMessage.originalMessage().accountNumber());
    }

    /**
     * Record DLT processing time
     */
    public Timer.Sample startDltProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDltProcessingTime(Timer.Sample sample, String outcome) {
        sample.stop(Timer.builder(DLT_PROCESSING_TIME)
            .tag("outcome", outcome)
            .description("Time taken to process DLT messages")
            .register(meterRegistry));
    }

    /**
     * Record message processing failure before it goes to DLT
     */
    public void recordMessageFailure(String messageType, String errorType) {
        Counter.builder(MESSAGE_FAILURE_COUNTER)
            .tag("message.type", messageType)
            .tag("error.type", errorType)
            .description("Number of message processing failures")
            .register(meterRegistry)
            .increment();

        log.debug("Message failure metric recorded: type={}, error={}", messageType, errorType);
    }
}