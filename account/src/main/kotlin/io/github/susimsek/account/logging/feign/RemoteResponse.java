package io.github.susimsek.account.logging.feign;

import feign.Response;
import io.github.susimsek.account.logging.core.HttpResponse;
import io.github.susimsek.account.logging.core.Origin;
import io.github.susimsek.account.logging.utils.HeaderUtils;
import org.springframework.http.HttpHeaders;

record RemoteResponse(
    int status,
    HttpHeaders headers,
    byte[] body) implements HttpResponse {
    public static RemoteResponse create(Response response, byte[] body) {
        return new RemoteResponse(
            response.status(),
            HeaderUtils.toHeaders(response.headers()),
            body
        );
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public String getProtocolVersion() {
        return "HTTP/1.1";
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public byte[] body() {
        return body != null ? body : new byte[0];
    }
}