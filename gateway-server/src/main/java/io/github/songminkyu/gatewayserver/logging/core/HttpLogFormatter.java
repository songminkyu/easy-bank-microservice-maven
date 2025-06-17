package io.github.songminkyu.gatewayserver.logging.core;

import java.io.IOException;

public interface HttpLogFormatter {

    String format(HttpRequest request) throws IOException;

    String format(HttpResponse response) throws IOException;

}