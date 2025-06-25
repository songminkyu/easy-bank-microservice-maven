package io.github.songminkyu.account.logging.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

public interface HttpMessage {

    default String getProtocolVersion() {
        return "HTTP/1.1";
    }

    Origin getOrigin();

    HttpHeaders headers();

    @Nullable
    default String getContentType() {
        return
            Optional.ofNullable(headers().getContentType())
                .map(MediaType::toString)
                .orElse(null);
    }


    byte[] body() throws IOException;

    default String getBodyAsString() throws IOException {
        return Optional.ofNullable(body())
            .map(bytes -> new String(bytes, StandardCharsets.UTF_8))
            .orElse("");
    }
}