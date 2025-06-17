package io.github.songminkyu.card.logging.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.songminkyu.card.logging.core.HttpMessage;
import io.github.songminkyu.card.logging.core.StructuredHttpLogFormatter;
import io.github.songminkyu.card.logging.utils.HeaderUtils;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public final class JsonHttpLogFormatter implements StructuredHttpLogFormatter {

    private final ObjectMapper om;

    public JsonHttpLogFormatter(ObjectMapper mapper) {
        var om = mapper.copy();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        this.om = om;
    }

    public Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        var contentType = message.getContentType();
        var body = message.getBodyAsObject() != null ? message.getBodyAsObject() :
            message.getBodyAsString();
        if (body == null) {
            return Optional.empty();
        }
        if (contentType != null && HeaderUtils.isContentTypeSupported(contentType)) {
            if (body instanceof String s) {
                return Optional.of(om.readValue(s, Object.class));
            }
        }
        return Optional.of(body);
    }

    public String format(final Map<String, Object> content) throws IOException {
        return om.writeValueAsString(content);
    }
}