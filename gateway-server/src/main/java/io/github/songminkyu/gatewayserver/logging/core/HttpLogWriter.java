package io.github.songminkyu.gatewayserver.logging.core;

import java.io.IOException;

public interface HttpLogWriter {

    void write(String payload) throws IOException;

}