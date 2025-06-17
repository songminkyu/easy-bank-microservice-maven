package io.github.songminkyu.gatewayserver.logging.core;

public interface HttpResponse extends HttpMessage {
    int status();
}