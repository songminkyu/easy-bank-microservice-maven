package io.github.songminkyu.account.logging.restclient;

import io.github.songminkyu.account.logging.core.HttpRequest;
import io.github.songminkyu.account.logging.core.Origin;
import java.net.URI;
import org.springframework.http.HttpHeaders;

public class RestClientRequest implements HttpRequest {

    org.springframework.http.HttpRequest request;
    private final URI uri;
    private final HttpHeaders headers;
    private final byte[] body;

    public RestClientRequest(org.springframework.http.HttpRequest request, byte[] body) {
        this.request = request;
        this.headers = request.getHeaders();
        this.uri = request.getURI();
        this.body = body;
    }

    @Override
    public String getRemote() {
        return "localhost";
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
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

    @Override
    public byte[] body() {
        return body;
    }
}