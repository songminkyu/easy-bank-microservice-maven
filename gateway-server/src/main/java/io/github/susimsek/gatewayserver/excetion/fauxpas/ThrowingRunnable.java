package io.github.susimsek.gatewayserver.excetion.fauxpas;

import lombok.SneakyThrows;


@SuppressWarnings("checkstyle:InterfaceTypeParameterName")
public interface ThrowingRunnable<T extends Throwable> extends Runnable {

    void tryRun() throws T;

    @Override
    @SneakyThrows
    default void run() {
        tryRun();
    }

}