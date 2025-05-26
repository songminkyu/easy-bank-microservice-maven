package io.github.susimsek.account.logging.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public final class DefaultHttpLogWriter implements HttpLogWriter {
    @Override
    public void write(String payload) {
        log.info(payload);
    }
}