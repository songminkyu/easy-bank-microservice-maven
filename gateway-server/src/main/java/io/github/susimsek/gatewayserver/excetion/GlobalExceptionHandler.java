package io.github.susimsek.gatewayserver.excetion;


import static io.github.susimsek.gatewayserver.excetion.ErrorConstants.ERR_INTERNAL_SERVER;
import static io.github.susimsek.gatewayserver.excetion.ErrorConstants.ERR_VALIDATION;
import static io.github.susimsek.gatewayserver.excetion.ErrorConstants.PROBLEM_VIOLATION_KEY;
import static java.util.stream.Collectors.toList;

import io.github.susimsek.gatewayserver.dto.Violation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected Mono<ResponseEntity<Object>> handleNotAcceptableStatusException(NotAcceptableStatusException ex,
                                                                              HttpHeaders headers,
                                                                              HttpStatusCode status,
                                                                              ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleUnsupportedMediaTypeStatusException(
        UnsupportedMediaTypeStatusException ex, HttpHeaders headers, HttpStatusCode status,
        ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
        headers.setAccept(ex.getSupportedMediaTypes());
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleMethodNotAllowedException(MethodNotAllowedException ex,
                                                                           HttpHeaders headers, HttpStatusCode status,
                                                                           ServerWebExchange exchange) {
        var methods = ex.getSupportedMethods();
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        if (CollectionUtils.isEmpty(methods)) {
            return handleExceptionInternal(ex, problem, null,
                HttpStatusCode.valueOf(problem.getStatus()), exchange);
        }
        headers.setAllow(methods);
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(
        WebExchangeBindException ex, HttpHeaders headers, HttpStatusCode status, ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ERR_VALIDATION);
        var fieldErrors = ex.getFieldErrors().stream().map(Violation::new);
        var globalErrors = ex.getGlobalErrors().stream().map(Violation::new);
        var violations = Stream.concat(fieldErrors, globalErrors).collect(toList());
        problem.setProperty(PROBLEM_VIOLATION_KEY, violations);
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    protected Mono<ResponseEntity<Object>> handleConstraintViolationException(ConstraintViolationException ex,
                                                                              ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ERR_VALIDATION);
        val violations = ex.getConstraintViolations().stream().map(Violation::new);
        problem.setProperty(PROBLEM_VIOLATION_KEY, violations);

        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public Mono<ResponseEntity<Object>> handleEntityNotFound(
        EntityNotFoundException ex,
        ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Object>> handleAll(
        Exception ex,
        ServerWebExchange exchange) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), exchange);
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleExceptionInternal(Exception ex,
                                                                   @Nullable Object body,
                                                                   @Nullable HttpHeaders headers,
                                                                   HttpStatusCode status,
                                                                   ServerWebExchange exchange) {
        if (status.is5xxServerError()) {
            log.error("An exception occured, which will cause a {} response", status, ex);
            var problem = ProblemDetail.forStatusAndDetail(status, ERR_INTERNAL_SERVER);
            return super.handleExceptionInternal(ex, problem, headers, status, exchange);
        } else if (status.is4xxClientError()) {
            log.warn("An exception occured, which will cause a {} response", status, ex);
        } else {
            log.debug("An exception occured, which will cause a {} response", status, ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, exchange);
    }
}