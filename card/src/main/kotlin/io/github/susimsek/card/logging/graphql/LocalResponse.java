package io.github.susimsek.card.logging.graphql;

import io.github.susimsek.card.logging.core.HttpResponse;
import io.github.susimsek.card.logging.core.Origin;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public final class LocalResponse  implements HttpResponse {

    private final WebGraphQlResponse response;
    private final HttpHeaders headers;

    private final Object body;


    LocalResponse(final WebGraphQlResponse response) {
        this.response = response;
        this.headers = response.getResponseHeaders();
        this.body = response.toMap();
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
        return HttpStatus.OK.value();
    }

    @Override
    public byte[] body() {
        return new byte[0];
    }

    @Override
    public Object getBodyAsObject() {
        return body;
    }
}