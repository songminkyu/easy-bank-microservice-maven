package io.github.songminkyu.gatewayserver.excetion.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.songminkyu.gatewayserver.excetion.utils.AdviceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SecurityProblemSupport implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

    private final ObjectMapper mapper;

    @Override
    public Mono<Void> commence(final ServerWebExchange exchange, final AuthenticationException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, ex.getMessage());
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return AdviceUtils.setHttpResponse(
            new ResponseEntity<>(problem, headers, HttpStatusCode.valueOf(problem.getStatus())),
            exchange, mapper);
    }

    @Override
    public Mono<Void> handle(final ServerWebExchange exchange, final AccessDeniedException ex) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, ex.getMessage());
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return AdviceUtils.setHttpResponse(
            new ResponseEntity<>(problem, headers, HttpStatusCode.valueOf(problem.getStatus())),
            exchange, mapper);
    }

}