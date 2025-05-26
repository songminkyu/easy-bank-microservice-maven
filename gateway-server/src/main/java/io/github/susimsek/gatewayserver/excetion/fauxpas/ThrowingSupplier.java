package io.github.susimsek.gatewayserver.excetion.fauxpas;

import java.util.function.Supplier;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingSupplier<T, X extends Throwable> extends Supplier<T> {

    T tryGet() throws X;

    @Override
    @SneakyThrows
    default T get() {
        return tryGet();
    }

}