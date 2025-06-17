package io.github.songminkyu.account.logging.webclient;

import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

@UtilityClass
class DataBufferCopyUtils {

    Publisher<? extends DataBuffer> wrapAndBuffer(Publisher<? extends DataBuffer> body, Consumer<byte[]> copyConsumer) {
        return DataBufferUtils
            .join(body)
            .defaultIfEmpty(DefaultDataBufferFactory.sharedInstance.wrap(new byte[0]))
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                DefaultDataBuffer wrappedDataBuffer = DefaultDataBufferFactory.sharedInstance.wrap(bytes);
                copyConsumer.accept(bytes);
                return wrappedDataBuffer;
            });
    }
}