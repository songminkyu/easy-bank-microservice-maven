package io.github.songminkyu.account.logging.webclient;

import io.github.songminkyu.account.logging.core.HttpResponse;
import io.github.songminkyu.account.logging.core.Origin;
import org.springframework.http.HttpHeaders;

final class ClientResponse implements HttpResponse {

    private final org.springframework.web.reactive.function.client.ClientResponse response;

    private final HttpHeaders headers;
    private byte[] body;

    public ClientResponse(org.springframework.web.reactive.function.client.ClientResponse response) {
        this.response = response;
        this.headers = response.headers().asHttpHeaders();
    }

    @Override
    public int status() {
        return response.statusCode().value();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
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