package io.github.songminkyu.gatewayserver.logging.reactive;

import static io.github.songminkyu.gatewayserver.excetion.fauxpas.FauxPas.throwingRunnable;

import io.github.songminkyu.gatewayserver.logging.core.Sink;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"NullableProblems"})
public class LoggingFilter implements WebFilter, Ordered {

    private final Sink sink;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerRequest serverRequest = new ServerRequest(exchange.getRequest());
        ServerResponse serverResponse = new ServerResponse(exchange.getResponse());
        return Mono
            .just(exchange)
            .map(e -> e
                .mutate()
                .request(new BufferingServerHttpRequest(e.getRequest(), serverRequest,
                    throwingRunnable(() -> sink.write(serverRequest))))
                .response(new BufferingServerHttpResponse(e.getResponse(), serverResponse,
                    throwingRunnable(() -> sink.write(serverResponse))))
                .build()
            )
            .flatMap(chain::filter)
            .then();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}