package io.github.songminkyu.card.exception.fauxpas;

import lombok.experimental.UtilityClass;

@UtilityClass
@SuppressWarnings("checkstyle:MethodTypeParameterName")
public class FauxPas {

    public static <X extends Throwable> ThrowingRunnable<X> throwingRunnable(
        final ThrowingRunnable<X> runnable) {
        return runnable;
    }

    public static <T, X extends Throwable> ThrowingConsumer<T, X> throwingConsumer(
        final ThrowingConsumer<T, X> consumer) {
        return consumer;
    }

    public static <T, R, X extends Throwable> ThrowingFunction<T, R, X> throwingFunction(
        final ThrowingFunction<T, R, X> function) {
        return function;
    }


    public static <T, X extends Throwable> ThrowingSupplier<T, X> throwingSupplier(
        final ThrowingSupplier<T, X> supplier) {
        return supplier;
    }

    public static <T, X extends Throwable> ThrowingUnaryOperator<T, X> throwingUnaryOperator(
        final ThrowingUnaryOperator<T, X> operator) {
        return operator;
    }

    public static <T, X extends Throwable> ThrowingPredicate<T, X> throwingPredicate(
        final ThrowingPredicate<T, X> predicate) {
        return predicate;
    }
}
