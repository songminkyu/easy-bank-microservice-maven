package io.github.susimsek.account.exception;

import static io.github.susimsek.account.exception.ErrorConstants.ERR_INTERNAL_SERVER;
import static io.github.susimsek.account.exception.ErrorConstants.ERR_VALIDATION;
import static io.github.susimsek.account.exception.ErrorConstants.PROBLEM_VIOLATION_KEY;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import io.github.susimsek.account.dto.Violation;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.GraphQlClientException;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.CollectionUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers, HttpStatusCode status,
                                                                      WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_ACCEPTABLE, ex.getMessage());
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                                     HttpHeaders headers, HttpStatusCode status,
                                                                     WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
        headers.setAccept(ex.getSupportedMediaTypes());
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatusCode status,
                                                                         WebRequest request) {
        var methods = Set.of(ex.getSupportedMethods());
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        if (CollectionUtils.isEmpty(methods)) {
            return handleExceptionInternal(ex, problem, null,
                HttpStatusCode.valueOf(problem.getStatus()), request);
        }
        headers.setAllow(requireNonNull(ex.getSupportedHttpMethods()));
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ERR_VALIDATION);
        var fieldErrors = ex.getFieldErrors().stream().map(Violation::new);
        var globalErrors = ex.getGlobalErrors().stream().map(Violation::new);
        var violations = Stream.concat(fieldErrors, globalErrors).collect(toList());
        problem.setProperty(PROBLEM_VIOLATION_KEY, violations);
        return handleExceptionInternal(ex, problem, headers,
            HttpStatusCode.valueOf(problem.getStatus()), request);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
                                                                        WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ERR_VALIDATION);
        val violations = ex.getConstraintViolations().stream().map(Violation::new);
        problem.setProperty(PROBLEM_VIOLATION_KEY, violations);

        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ResponseEntity<Object> handleAuthentication(AuthenticationException ex,
                                                          WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex,
                                                        WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFound(
        EntityNotFoundException ex,
        WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Object> handleCompletionException(
        CompletionException ex,
        WebRequest webRequest) {
        if (ex.getCause() instanceof CustomerAlreadyExistsException e) {
            return handleCustomerAlreadyExistsException(e, webRequest);
        }
        return handleAll(ex, webRequest);
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
        jakarta.persistence.EntityNotFoundException ex,
        WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }


    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<Object> handleCustomerAlreadyExistsException(
        CustomerAlreadyExistsException ex,
        WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }

    @ExceptionHandler(FieldAccessException.class)
    public ResponseEntity<Object> handleFieldAccessException(
        FieldAccessException ex,
        WebRequest webRequest) {
        var responseErrorOptional = Optional.of(ex.getResponse()
                .getErrors())
            .filter(responseErrors -> !CollectionUtils.isEmpty(responseErrors))
            .map(responseErrors -> responseErrors.get(0));
        if (responseErrorOptional.isPresent()) {
            var responseError = responseErrorOptional.get();
            return handleGraphQlClientException(ex, responseError, webRequest);
        }
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(
        Exception ex,
        WebRequest webRequest) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("An exception occured, which will cause a {} response", status, ex);
            var problem = ProblemDetail.forStatusAndDetail(status, ERR_INTERNAL_SERVER);
            return super.handleExceptionInternal(ex, problem, headers, status, request);
        } else if (status.is4xxClientError()) {
            log.warn("An exception occured, which will cause a {} response", status, ex);
        } else {
            log.debug("An exception occured, which will cause a {} response", status, ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    protected ResponseEntity<Object> handleGraphQlClientException(
        GraphQlClientException ex,
        ResponseError responseError,
        WebRequest webRequest) {
        var extensions = responseError.getExtensions();
        var classification = extensions.get("classification");
        var problem = Arrays.stream(ErrorType.values())
            .filter(errorType -> errorType.name().equals(classification))
            .findFirst()
            .map(errorType -> switch (errorType) {
                case BAD_REQUEST -> ProblemDetail.forStatusAndDetail(
                    HttpStatus.BAD_REQUEST, ex.getMessage());
                case NOT_FOUND -> ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND, ex.getMessage());
                default -> ProblemDetail.forStatusAndDetail(
                    HttpStatus.INTERNAL_SERVER_ERROR, ERR_INTERNAL_SERVER);
            }).orElse(ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, ERR_INTERNAL_SERVER));
        return handleExceptionInternal(ex, problem, null,
            HttpStatusCode.valueOf(problem.getStatus()), webRequest);
    }
}