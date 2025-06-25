package io.github.songminkyu.gatewayserver.logging.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ByteStreams {

    public byte[] toByteArray(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    void copy(final InputStream from, final OutputStream to) throws IOException {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        final byte[] buf = new byte[4096];
        int bytesRead;

        while ((bytesRead = from.read(buf)) != -1) {
            to.write(buf, 0, bytesRead);
        }
    }

}