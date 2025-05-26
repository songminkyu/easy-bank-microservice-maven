package io.github.susimsek.card.exception.fauxpas;

import java.io.IOException;
import java.util.function.Supplier;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingSupplier<T, X extends Throwable> extends Supplier<T> {

    T tryGet() throws X, IOException;

    @Override
    @SneakyThrows
    default T get() {
        return tryGet();
    }

}