package io.github.songminkyu.gatewayserver.logging.reactive;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

@SuppressWarnings({"NullableProblems"})
@Slf4j
public class BufferingServerHttpRequest extends ServerHttpRequestDecorator {

    private final ServerRequest serverRequest;
    private final Runnable writeHook;

    BufferingServerHttpRequest(ServerHttpRequest delegate, ServerRequest serverRequest, Runnable writeHook) {
        super(delegate);
        this.serverRequest = serverRequest;
        this.writeHook = writeHook;
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return Flux.from(bufferingWrap(super.getBody()));
    }

    private Publisher<? extends DataBuffer> bufferingWrap(Publisher<? extends DataBuffer> body) {
        if (serverRequest.shouldBuffer()) {
            return Flux
                .from(DataBufferCopyUtils.wrapAndBuffer(body, serverRequest::buffer))
                .doOnComplete(writeHook);
        } else {
            return body;
        }
    }
}