package io.github.susimsek.card.exception.fauxpas;

import java.util.function.UnaryOperator;

@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingUnaryOperator<T, X extends Throwable> extends ThrowingFunction<T, T, X>, UnaryOperator<T> {

}