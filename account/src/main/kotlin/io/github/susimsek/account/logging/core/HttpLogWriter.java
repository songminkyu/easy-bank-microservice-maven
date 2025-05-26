package io.github.susimsek.account.logging.core;

import java.io.IOException;

public interface HttpLogWriter {

    void write(String payload) throws IOException;

}