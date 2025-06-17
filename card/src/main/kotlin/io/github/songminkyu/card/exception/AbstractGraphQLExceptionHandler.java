package io.github.songminkyu.card.exception;

import static io.github.songminkyu.card.exception.ErrorConstants.ERR_INTERNAL_SERVER;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.ErrorType;

@Slf4j
public abstract class AbstractGraphQLExceptionHandler {
    private static final String ERROR_LOG_MSG = "An exception occured, which will cause response";

    protected GraphQLError handleValidationException(DataFetchingEnvironment env, Exception ex) {
        return handleExceptionInternal(ex, ErrorType.BAD_REQUEST, env );
    }

    protected GraphQLError handleExceptionInternal(Throwable ex,
                                                   ErrorType errorType,
                                                   DataFetchingEnvironment env) {
        if (errorType == ErrorType.INTERNAL_ERROR) {
            log.error(ERROR_LOG_MSG, ex);
            return createGraphQLError(env, ERR_INTERNAL_SERVER,  errorType);
        } else if (errorType == ErrorType.BAD_REQUEST) {
            log.warn(ERROR_LOG_MSG, ex);
        } else {
            log.debug(ERROR_LOG_MSG, ex);
        }
        return createGraphQLError(env, ex.getMessage(),  errorType);
    }

    protected GraphQLError createGraphQLError(
        DataFetchingEnvironment env,
        String errorMessage,
        ErrorType errorType) {
        return GraphqlErrorBuilder.newError(env)
            .message(errorMessage)
            .errorType(errorType).build();
    }
}
