package io.github.songminkyu.card.logging.core;

import java.io.IOException;

public interface HttpLogWriter {

    void write(String payload) throws IOException;

}