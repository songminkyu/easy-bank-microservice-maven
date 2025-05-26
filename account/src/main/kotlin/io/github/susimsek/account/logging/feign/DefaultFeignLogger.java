package io.github.susimsek.account.logging.feign;

import feign.Request;
import feign.Response;
import io.github.susimsek.account.logging.core.HttpRequest;
import io.github.susimsek.account.logging.core.HttpResponse;
import io.github.susimsek.account.logging.core.Sink;
import io.github.susimsek.account.logging.utils.ByteStreams;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.Generated;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultFeignLogger extends feign.Logger {

    private final Sink sink;

    @Override
    @Generated
    protected void log(String configKey, String format, Object... args) {
        /* no-op, logging is delegated */
    }

    @Override
    protected void logRetry(String configKey, Level logLevel) {
        /* no-op, logging is delegated */
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        /* no-op, logging is delegated */
        return ioe;
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        final HttpRequest httpRequest = LocalRequest.create(request);
        try {
            sink.write(httpRequest);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) {
        try {
            byte[] body = response.body() != null ? ByteStreams.toByteArray(response.body().asInputStream()) : null;

            final HttpResponse httpResponse = RemoteResponse.create(response, body);
            sink.write(httpResponse);

            return Response.builder()
                .status(response.status())
                .request(response.request())
                .reason(response.reason())
                .headers(response.headers())
                .body(body)
                .build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}