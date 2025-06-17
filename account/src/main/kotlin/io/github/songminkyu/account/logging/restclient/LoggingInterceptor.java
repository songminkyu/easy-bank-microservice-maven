package io.github.songminkyu.account.logging.restclient;

import io.github.songminkyu.account.logging.core.HttpResponse;
import io.github.songminkyu.account.logging.core.Sink;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

@Slf4j
@RequiredArgsConstructor
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    private final Sink sink;

    @NonNull
    @Override
    public ClientHttpResponse intercept(
        @NonNull HttpRequest clientRequest,
        @NonNull byte[] body,
        @NonNull ClientHttpRequestExecution execution) {
        try {
            logRequest(clientRequest, body);
            ClientHttpResponse response = execution.execute(clientRequest, body);
            logResponse(response);
            return response;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException {
        final io.github.songminkyu.account.logging.core.HttpRequest httpRequest =
            new RestClientRequest(request, body);
        sink.write(httpRequest);
    }

    private void logResponse(ClientHttpResponse response) throws IOException {
        HttpResponse clientResponse = new RestClientResponse(response);
        sink.write(clientResponse);
    }
}