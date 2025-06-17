package io.github.songminkyu.account.logging.core;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class DefaultSink implements Sink {

    private final HttpLogFormatter formatter;
    private final HttpLogWriter writer;

    @Override
    public void write(final HttpRequest request)
        throws IOException {
        writer.write(formatter.format(request));
    }

    @Override
    public void write(final HttpResponse response)
        throws IOException {
        writer.write(formatter.format(response));
    }

}