package io.github.susimsek.card.logging.core;

import java.io.IOException;

public interface Sink {

    void write(final HttpRequest request) throws IOException;

    void write(final HttpResponse response) throws IOException;
}