package io.github.susimsek.gatewayserver.filter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class FilterUtility {

    public static final String CORRELATION_ID = "X-Correlation-Id";

    public String getCorrelationId(HttpHeaders requestHeaders) {
        var optionalHeader = Optional.ofNullable(requestHeaders.get(CORRELATION_ID));
        if (optionalHeader.isPresent()) {
            List<String> requestHeaderList = optionalHeader.get();
            return requestHeaderList.iterator().next();
        } else {
            return null;
        }
    }

    public boolean hasCorrelationId(HttpHeaders header) {
        return header.containsKey(CORRELATION_ID);
    }

    public ServerWebExchange setRequestHeader(ServerWebExchange exchange, String name, String value) {
        return exchange.mutate().request(exchange.getRequest().mutate().header(name, value).build()).build();
    }

    public ServerWebExchange setCorrelationId(ServerWebExchange exchange, String correlationId) {
        return this.setRequestHeader(exchange, CORRELATION_ID, correlationId);
    }

    public String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

}