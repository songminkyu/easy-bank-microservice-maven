package io.github.susimsek.account.logging.core;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;

public interface StructuredHttpLogFormatter extends HttpLogFormatter {

    @Override
    default String format(final HttpResponse response)
        throws IOException {
        return format(prepare(response));
    }

    @Override
    default String format(final HttpRequest request) throws IOException {
        return format(prepare(request));
    }

    String format(Map<String, Object> content) throws IOException;

    default Map<String, Object> prepare(final HttpRequest request)
        throws IOException {

        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", request.getOrigin().name().toLowerCase(Locale.ROOT));
        content.put("type", "request");
        content.put("protocol", request.getProtocolVersion());
        content.put("remote", request.getRemote());
        content.put("method", request.getMethod());
        content.put("uri", request.getRequestUri());
        content.put("host", request.getHost());
        content.put("path", request.getPath());

        prepareHeaders(request).ifPresent(headers -> content.put("headers", headers));
        prepareBody(request).ifPresent(body -> content.put("body", body));

        return content;
    }


    default Map<String, Object> prepare(final HttpResponse response) throws IOException {
        final Map<String, Object> content = new LinkedHashMap<>();

        content.put("origin", response.getOrigin().name().toLowerCase(Locale.ROOT));
        content.put("type", "response");
        content.put("protocol", response.getProtocolVersion());
        content.put("status", response.status());

        prepareHeaders(response).ifPresent(headers -> content.put("headers", headers));
        prepareBody(response).ifPresent(body -> content.put("body", body));

        return content;
    }

    default Optional<HttpHeaders> prepareHeaders(final HttpMessage message) {
        var headers = message.headers();
        return Optional.ofNullable(headers.isEmpty() ? null : headers);
    }

    default Optional<Object> prepareBody(final HttpMessage message) throws IOException {
        final String body = message.getBodyAsString();
        return Optional.ofNullable(body.isEmpty() ? null : body);
    }

}