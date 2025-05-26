package io.github.susimsek.gatewayserver.excetion.fauxpas;

import java.util.function.Consumer;
import lombok.SneakyThrows;

@FunctionalInterface
@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingConsumer<T, X extends Throwable> extends Consumer<T> {

    void tryAccept(T t) throws X;

    @Override
    @SneakyThrows
    default void accept(final T t) {
        tryAccept(t);
    }

}