package io.github.susimsek.gatewayserver.logging.core;

public interface HttpResponse extends HttpMessage {
    int status();
}