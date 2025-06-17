package io.github.songminkyu.account.logging.servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

interface HttpFilter extends Filter {

    @Override
    default void init(final FilterConfig filterConfig) {
        // no initialization needed by default
    }

    @Override
    default void doFilter(final ServletRequest request, final ServletResponse response,
                          final FilterChain chain) throws ServletException, IOException {

        if (!(request instanceof HttpServletRequest httpRequest)
            || !(response instanceof HttpServletResponse httpResponse)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " only supports HTTP");
        }

        doFilter(httpRequest, httpResponse, chain);
    }

    void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                  final FilterChain chain) throws ServletException, IOException;

    @Override
    default void destroy() {
        // no deconstruction needed by default
    }

}