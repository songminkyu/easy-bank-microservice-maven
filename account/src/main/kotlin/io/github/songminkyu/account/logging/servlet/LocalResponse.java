package io.github.songminkyu.account.logging.servlet;

import io.github.songminkyu.account.logging.core.HttpResponse;
import io.github.songminkyu.account.logging.core.Origin;
import io.github.songminkyu.account.logging.utils.HeaderUtils;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;

public final class LocalResponse extends HttpServletResponseWrapper implements HttpResponse {

    private final String protocolVersion;
    private final HttpHeaders headers;

    private final Tee tee;

    LocalResponse(final HttpServletResponse response, final String protocolVersion) throws IOException {
        super(response);
        this.protocolVersion = protocolVersion;
        this.headers = HeaderUtils.toHeaders(
            response.getHeaderNames(),
            response::getHeaders,
            response::getHeader);
        this.tee = new Tee(response.getOutputStream());
    }

    @Override
    public Origin getOrigin() {
        return Origin.LOCAL;
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public HttpHeaders headers() {
        return headers;
    }

    @Override
    public int status() {
        return getStatus();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return tee.getOutputStream();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return tee.getWriter(() -> StandardCharsets.UTF_8);
    }

    @Override
    public void flushBuffer() throws IOException {
        tee.flush();
        super.flushBuffer();
    }

    @Override
    public byte[] body() {
        return tee.getBytes();
    }

    private static class Tee {

        private final ByteArrayOutputStream branch;
        private final TeeServletOutputStream output;

        private PrintWriter writer;
        private byte[] bytes;

        private Tee(final ServletOutputStream original) {
            this.branch = new ByteArrayOutputStream();
            this.output = new TeeServletOutputStream(original, branch);
        }

        ServletOutputStream getOutputStream() {
            return output;
        }

        PrintWriter getWriter(final Supplier<Charset> charset) {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(output, charset.get()));
            }
            return writer;
        }

        void flush() throws IOException {
            if (writer == null) {
                output.flush();
            } else {
                writer.flush();
            }
        }

        byte[] getBytes() {
            if (bytes == null) {
                bytes = branch.toByteArray();
            }
            return bytes;
        }
    }

    @AllArgsConstructor
    private static class TeeServletOutputStream extends ServletOutputStream {

        private final ServletOutputStream original;
        private final OutputStream branch;

        @Override
        public void write(final int b) throws IOException {
            original.write(b);
            branch.write(b);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            original.write(b, off, len);
            branch.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            original.flush();
            branch.flush();
        }

        @Override
        public void close() throws IOException {
            original.close();
            branch.close();
        }

        @Override
        public boolean isReady() {
            return original.isReady();
        }

        @Override
        public void setWriteListener(final WriteListener listener) {
            original.setWriteListener(listener);
        }

    }
}