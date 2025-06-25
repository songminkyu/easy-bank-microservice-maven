package io.github.songminkyu.account.logging.core;

import java.io.IOException;

public interface HttpLogWriter {

    void write(String payload) throws IOException;

}