package io.github.susimsek.card.exception.security;

import static java.util.Collections.singletonList;
import static reactor.core.publisher.Mono.fromCallable;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.github.susimsek.card.exception.AbstractGraphQLExceptionHandler;
import java.util.List;
import java.util.Optional;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.execution.SubscriptionExceptionResolver;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GraphQLSecurityExceptionResolver extends AbstractGraphQLExceptionHandler
    implements SubscriptionExceptionResolver {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    private GraphQLError resolveUnauthorized() {
        return GraphqlErrorBuilder.newError()
            .errorType(ErrorType.UNAUTHORIZED)
            .message(HttpStatus.UNAUTHORIZED.getReasonPhrase())
            .build();
    }

    private GraphQLError resolveAccessDenied(SecurityContext securityContext) {
        if (trustResolver.isAnonymous(securityContext.getAuthentication())) {
            return resolveUnauthorized();
        } else {
            return GraphqlErrorBuilder.newError()
                .errorType(ErrorType.FORBIDDEN)
                .message(HttpStatus.FORBIDDEN.getReasonPhrase())
                .build();
        }
    }

    @Override
    public Mono<List<GraphQLError>> resolveException(Throwable exception) {
        if (exception instanceof AuthenticationException) {
            var error = resolveUnauthorized();
            return Mono.just(singletonList(error));
        }
        if (exception instanceof AccessDeniedException) {
            return ReactiveSecurityContextHolder.getContext()
                .map(context -> singletonList(resolveAccessDenied(context)))
                .switchIfEmpty(fromCallable(() -> singletonList(resolveUnauthorized())));
        }
        return Mono.empty();
    }

    public GraphQLError resolveException(Throwable exception, DataFetchingEnvironment env) {
        if (exception instanceof AuthenticationException) {
            return resolveUnauthorized();
        }
        if (exception instanceof AccessDeniedException) {
            return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(this::resolveAccessDenied)
                .orElse(resolveUnauthorized());
        }
        return handleExceptionInternal(exception, ErrorType.INTERNAL_ERROR, env);
    }
}
