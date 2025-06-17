package io.github.songminkyu.card.exception;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import io.github.songminkyu.card.exception.security.GraphQLSecurityExceptionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GraphQLGlobalExceptionHandler  extends AbstractGraphQLExceptionHandler {

    private final GraphQLSecurityExceptionResolver securityExceptionResolver;

    @GraphQlExceptionHandler
    public GraphQLError handleEntityNotFound(
        EntityNotFoundException ex,
        DataFetchingEnvironment  env) {
        return handleExceptionInternal(ex, ErrorType.NOT_FOUND, env);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleEntityNotFound(
        jakarta.persistence.EntityNotFoundException ex,
        DataFetchingEnvironment  env) {
        return handleExceptionInternal(ex, ErrorType.NOT_FOUND, env);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleCardAlreadyExistsException(
        CardAlreadyExistsException ex,
        DataFetchingEnvironment  env) {
        return handleValidationException(env, ex);
    }

    @GraphQlExceptionHandler({
        AuthenticationException.class,
        AccessDeniedException.class
    })
    public GraphQLError handleSecurityException(
        Exception ex,
        DataFetchingEnvironment  env) {
        return securityExceptionResolver.resolveException(ex, env);
    }

    @GraphQlExceptionHandler
    public GraphQLError handleAll(
        Exception ex,
        DataFetchingEnvironment  env) {
        return handleExceptionInternal(ex, ErrorType.INTERNAL_ERROR, env);
    }
}
