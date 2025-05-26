package io.github.susimsek.account.logging.core;

import java.net.URI;
import java.util.Optional;

public interface HttpRequest extends HttpMessage {

    String getRemote();

    String getMethod();

    URI getRequestUri();

    default String getHost() {
        return Optional.of(getRequestUri())
            .map(URI::getHost)
            .orElse("");
    }

    default String getPath() {
        return Optional.of(getRequestUri())
            .map(URI::getPath)
            .orElse("");
    }

    default String getQuery() {
        return Optional.of(getRequestUri())
            .map(URI::getQuery)
            .orElse("");
    }

}