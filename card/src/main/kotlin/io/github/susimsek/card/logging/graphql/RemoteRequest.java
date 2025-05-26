package io.github.susimsek.card.logging.graphql;


import graphql.ExecutionInput;
import io.github.susimsek.card.logging.core.HttpRequest;
import io.github.susimsek.card.logging.core.Origin;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class RemoteRequest implements HttpRequest {

    private final WebGraphQlRequest request;

    private final HttpHeaders headers;

    private final Object body;

    private final URI uri;

    private final ExecutionInput executionInput;

    public RemoteRequest(final WebGraphQlRequest request) {
        this.request = request;
        this.headers = request.getHeaders();
        this.uri = request.getUri().toUri();
        this.body = request.toMap();
        this.executionInput = request.toExecutionInput();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getRemote() {
        return "0:0:0:0:0:0:0:1";
    }

    @Override
    public String getMethod() {
        return HttpMethod.POST.name();
    }

    @Override
    public URI getRequestUri() {
        return uri;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }


    @Override
    public byte[] body() {
        return new byte[0];
    }

    @Override
    public Object getBodyAsObject() {
        return body;
    }

    @Override
    public Map<String, Object> getAdditionalContent() {
        final Map<String, Object> content = new LinkedHashMap<>();
        content.put("query", executionInput.getQuery());
        content.put("variables", executionInput.getVariables());
        return content;
    }
}