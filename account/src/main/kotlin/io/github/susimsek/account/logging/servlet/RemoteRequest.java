package io.github.susimsek.account.logging.servlet;

import io.github.susimsek.account.logging.core.HttpRequest;
import io.github.susimsek.account.logging.core.Origin;
import io.github.susimsek.account.logging.utils.ByteStreams;
import io.github.susimsek.account.logging.utils.HeaderUtils;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;

public class RemoteRequest extends HttpServletRequestWrapper implements HttpRequest {
    private final Optional<AsyncListener> asyncListener = Optional.empty();

    private final HttpHeaders headers;

    private final byte[] body;

    private final URI uri;

    public RemoteRequest(final HttpServletRequest request) throws IOException {
        super(request);
        this.headers = HeaderUtils.toHeadersWithEnumHeaders(
            request.getHeaderNames(),
            request::getHeaders,
            request::getHeader);
        this.body = ByteStreams.toByteArray(request.getInputStream());
        this.uri = new ServletServerHttpRequest(request).getURI();
    }

    @Override
    public String getProtocolVersion() {
        return getProtocol();
    }

    @Override
    public Origin getOrigin() {
        return Origin.REMOTE;
    }

    @Override
    public String getRemote() {
        return getRemoteAddr();
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
    public ServletInputStream getInputStream() throws IOException {
        return new ServletInputStreamAdapter(new ByteArrayInputStream(body));
    }

    @Override
    public BufferedReader getReader() throws IOException {
        final InputStream stream = getInputStream();
        final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        return new BufferedReader(reader);
    }

    @Override
    public byte[] body() {
        return body;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        final AsyncContext asyncContext = super.startAsync();
        asyncListener.ifPresent(asyncContext::addListener);
        return asyncContext;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IllegalStateException {
        final AsyncContext asyncContext = super.startAsync(servletRequest, servletResponse);
        asyncListener.ifPresent(asyncContext::addListener);
        return asyncContext;
    }
}