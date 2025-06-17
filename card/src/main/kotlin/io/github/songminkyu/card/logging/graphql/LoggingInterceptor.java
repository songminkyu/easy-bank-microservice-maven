package io.github.songminkyu.card.logging.graphql;

import static io.github.songminkyu.card.exception.fauxpas.FauxPas.throwingRunnable;

import io.github.songminkyu.card.logging.core.Sink;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@SuppressWarnings({"NullableProblems"})
public class LoggingInterceptor implements WebGraphQlInterceptor {

    private final Sink sink;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        return chain.next(request).doOnNext(response -> {
            var graphQlRequest = new RemoteRequest(request);
            var graphQlResponse = new LocalResponse(response);
            throwingRunnable(() -> {
                sink.write(graphQlRequest);
                sink.write(graphQlResponse);
            }).run();
        });
    }
}