package io.github.songminkyu.gatewayserver.logging.reactive;

import io.github.songminkyu.gatewayserver.logging.core.HttpResponse;
import io.github.songminkyu.gatewayserver.logging.core.Origin;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

@RequiredArgsConstructor
public class ServerResponse implements HttpResponse {

    private final ServerHttpResponse response;

    private final HttpHeaders headers;
    private byte[] body;

    public ServerResponse(ServerHttpResponse response) {
        this.response = response;
        this.headers = response.getHeaders();
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public int status() {
        return Optional.ofNullable(response.getStatusCode())
            .orElse(HttpStatus.OK)
            .value();
    }

    boolean shouldBuffer() {
        return true;
    }

    void buffer(byte[] message) {
        this.body = message;
    }

    @Override
    public byte[] body() {
        return body;
    }
}