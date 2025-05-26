package io.github.susimsek.account.logging.webclient;

import static io.github.susimsek.account.exception.fauxpas.FauxPas.throwingConsumer;
import static io.github.susimsek.account.exception.fauxpas.FauxPas.throwingFunction;
import static org.springframework.http.HttpHeaders.TRANSFER_ENCODING;

import io.github.susimsek.account.logging.core.Sink;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@SuppressWarnings({"NullableProblems"})
public class LoggingExchangeFilterFunction implements ExchangeFilterFunction {

    private final Sink sink;

    @Override
    public Mono<org.springframework.web.reactive.function.client.ClientResponse> filter(
        org.springframework.web.reactive.function.client.ClientRequest request, ExchangeFunction next) {
        ClientRequest clientRequest = new ClientRequest(request);
        return next
            .exchange(org.springframework.web.reactive.function.client.ClientRequest
                .from(request)
                .body((outputMessage, context) -> request.body()
                    .insert(new BufferingClientHttpRequest(outputMessage, clientRequest), context))
                .build()
            )
            .flatMap(throwingFunction(response -> {
                sink.write(clientRequest);
                ClientResponse clientResponse = new ClientResponse(response);
                return Mono
                    .just(response)
                    .flatMap(it -> {
                        HttpHeaders responseHeaders = response.headers().asHttpHeaders();
                        if (clientResponse.shouldBuffer() && (responseHeaders.getContentLength() > 0
                            || responseHeaders.containsKey(TRANSFER_ENCODING))) {
                            return it
                                .bodyToMono(byte[].class)
                                .doOnNext(clientResponse::buffer)
                                .map(b -> response.mutate()
                                    .body(Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(b))).build())
                                .switchIfEmpty(Mono.just(it));
                        } else {
                            return Mono.just(it);
                        }
                    })
                    .doOnNext(throwingConsumer(b -> sink.write(clientResponse)));
            }));
    }
}