package io.github.songminkyu.gatewayserver.logging.reactive;

import io.github.songminkyu.gatewayserver.logging.core.HttpRequest;
import io.github.songminkyu.gatewayserver.logging.core.Origin;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

public class ServerRequest implements HttpRequest {

    private final ServerHttpRequest request;

    private final URI uri;

    private final HttpHeaders headers;
    private byte[] body;

    public ServerRequest(ServerHttpRequest request) {
        this.uri = request.getURI();
        this.request = request;
        this.headers = request.getHeaders();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public String getRemote() {
        return Optional.ofNullable(request.getRemoteAddress()).map(InetSocketAddress::toString).orElse("");
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public URI getRequestUri() {
        return uri;
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