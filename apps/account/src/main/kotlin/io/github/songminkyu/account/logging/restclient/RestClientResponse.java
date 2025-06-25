package io.github.songminkyu.account.logging.restclient;

import io.github.songminkyu.account.logging.core.HttpResponse;
import io.github.songminkyu.account.logging.core.Origin;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

public class RestClientResponse implements HttpResponse {

    private final ClientHttpResponse response;
    private final HttpHeaders headers;
    private final byte[] body;
    private final HttpStatusCode status;

    public RestClientResponse(ClientHttpResponse response) throws IOException {
        this.response = response;
        this.headers = response.getHeaders();
        this.body = response.getBody().readAllBytes();
        this.status = response.getStatusCode();
    }

    @Override
    public int status() {
        return status.value();
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
    public byte[] body() {
        return body;
    }
}