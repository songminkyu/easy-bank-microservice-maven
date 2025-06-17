package io.github.songminkyu.gatewayserver.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Order(1)
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestTraceFilter implements GlobalFilter {

    private final FilterUtility filterUtility;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
        if (filterUtility.hasCorrelationId(requestHeaders)) {
            log.debug("Tracked request with correlation id : {}",
                filterUtility.getCorrelationId(requestHeaders));
        } else {
            String correlationID = filterUtility.generateCorrelationId();
            exchange = filterUtility.setCorrelationId(exchange, correlationID);
            log.debug("correlation id generated in RequestTraceFilter : {}", correlationID);
        }
        return chain.filter(exchange);
    }

}