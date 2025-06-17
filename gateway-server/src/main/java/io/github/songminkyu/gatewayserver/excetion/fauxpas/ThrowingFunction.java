package io.github.songminkyu.gatewayserver.excetion.fauxpas;

import java.util.function.Function;
import lombok.SneakyThrows;

@FunctionalInterface
@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingFunction<T, R, X extends Throwable> extends Function<T, R> {

    R tryApply(T t) throws X;

    @Override
    @SneakyThrows
    default R apply(final T t) {
        return tryApply(t);
    }

}