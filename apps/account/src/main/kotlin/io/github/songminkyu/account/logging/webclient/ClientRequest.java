package io.github.songminkyu.account.logging.webclient;

import io.github.songminkyu.account.logging.core.HttpRequest;
import io.github.songminkyu.account.logging.core.Origin;
import java.net.URI;
import org.springframework.http.HttpHeaders;

final class ClientRequest implements HttpRequest {

    private final org.springframework.web.reactive.function.client.ClientRequest request;

    private final URI uri;
    private final HttpHeaders headers;
    private byte[] body;

    public ClientRequest(org.springframework.web.reactive.function.client.ClientRequest request) {
        this.uri = request.url();
        this.request = request;
        this.headers = request.headers();
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public URI getRequestUri() {
        return uri;
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
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