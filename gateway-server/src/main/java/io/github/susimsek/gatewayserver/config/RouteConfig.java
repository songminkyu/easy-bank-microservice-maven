package io.github.susimsek.gatewayserver.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@SuppressWarnings("java:S125")
public class RouteConfig {

    /*
    @Bean
    public RouteLocator eazyBankRouteConfig(
        RouteLocatorBuilder routeLocatorBuilder,
        KeyResolver userKeyResolver,
        RedisRateLimiter redisRateLimiter
        ) {
        return routeLocatorBuilder.routes()
            .route(p -> p
                .path("/eazybank/account/**")
                .filters(f -> f.rewritePath("/eazybank/account/(?<segment>.*)", "/${segment}")
                    .addResponseHeader("X-Response-Time", Instant.now().toString())
                    .circuitBreaker(config -> config.setName("accountCircuitBreaker")
                        .setFallbackUri("forward:/contactSupport")))
                .uri("lb://account:8080"))
            .route(p -> p
                .path("/eazybank/loan/**")
                .filters(f -> f.rewritePath("/eazybank/loan/(?<segment>.*)", "/${segment}")
                    .addResponseHeader("X-Response-Time", Instant.now().toString())
                    .retry(retryConfig -> retryConfig.setRetries(3)
                        .setMethods(HttpMethod.GET)
                        .setBackoff(
                            Duration.ofMillis(100),
                            Duration.ofMillis(1000),
                            2,true)))
                .uri("lb://loan:8090"))
            .route(p -> p
                .path("/eazybank/card/**")
                .filters(f -> f.rewritePath("/eazybank/card/(?<segment>.*)", "/${segment}")
                    .addResponseHeader("X-Response-Time", Instant.now().toString())
                    .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter)
                        .setKeyResolver(userKeyResolver)))
                .uri("lb://card:9000"))
            .build();
    }

     */

    @Bean
    KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
            .defaultIfEmpty("anonymous");
    }

}
