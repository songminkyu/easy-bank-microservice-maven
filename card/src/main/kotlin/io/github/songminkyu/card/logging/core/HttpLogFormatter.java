package io.github.songminkyu.card.logging.core;

import java.io.IOException;

public interface HttpLogFormatter {

    String format(HttpRequest request) throws IOException;

    String format(HttpResponse response) throws IOException;

}