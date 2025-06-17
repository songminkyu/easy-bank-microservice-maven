package io.github.songminkyu.card.logging.core;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
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

    default Map<String, Object> getAdditionalContent() {
        return Collections.EMPTY_MAP;
    }
}