package io.github.susimsek.account.logging.servlet;

import io.github.susimsek.account.logging.core.Sink;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class LoggingFilter implements HttpFilter {

    private final Sink sink;

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                         final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        chain.doFilter(request, response);

        write(request, response);
    }

    private void write(RemoteRequest request, LocalResponse response) throws IOException {
        sink.write(request);
        response.flushBuffer();
        sink.write(response);
    }
}