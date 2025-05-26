package io.github.susimsek.card.exception.fauxpas;

import java.util.function.Predicate;
import lombok.SneakyThrows;

@FunctionalInterface
@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingPredicate<T, X extends Throwable> extends Predicate<T> {

    boolean tryTest(T t) throws X;

    @Override
    @SneakyThrows
    default boolean test(final T t) {
        return tryTest(t);
    }

}