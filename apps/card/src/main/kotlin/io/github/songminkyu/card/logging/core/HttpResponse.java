package io.github.songminkyu.card.logging.core;

import java.util.Collections;
import java.util.Map;

public interface HttpResponse extends HttpMessage {
    int status();

    default Map<String, Object> getAdditionalContent() {
        return Collections.EMPTY_MAP;
    }
}