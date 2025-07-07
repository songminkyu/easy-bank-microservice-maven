package kuke.board.gatewayserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/contactSupport")
    public Mono<ResponseEntity<Map<String, Object>>> contactSupport(
            ServerWebExchange exchange) {

        // 원본 요청 URI 가져오기
        LinkedHashSet<URI> uris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        String originalPath = "/unknown";

        if (uris != null && !uris.isEmpty()) {
            originalPath = uris.iterator().next().getPath();
        }

        Map<String, Object> fallbackResponse = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 503,
                "error", "Service Unavailable",
                "message", "The service is temporarily unavailable. Please try again later.",
                "path", originalPath,
                "service", determineFailedService(originalPat   h)
        );

        log.error("Circuit breaker activated for original path: {}", originalPath);

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Content-Type", "application/json")
                .body(fallbackResponse));
    }

    private String determineFailedService(String path) {
        if (path.contains("/eazybank/accounts")) return "Account service";
        if (path.contains("/eazybank/cards")) return "Card Service";
        if (path.contains("/eazybank/loans")) return "Loan service";
        return "Service requested";
    }
}
