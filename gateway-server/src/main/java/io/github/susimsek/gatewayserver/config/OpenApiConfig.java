package io.github.susimsek.gatewayserver.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(RouteDefinitionLocator locator) {
        List<RouteDefinition> definitions = Optional.ofNullable(
            locator.getRouteDefinitions().collectList().block()).orElse(Collections.emptyList());
        return definitions.stream()
            .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
            .map(routeDefinition -> {
                String name = routeDefinition.getId().replace("-service", "");
                return GroupedOpenApi.builder().pathsToMatch("/" + name + "/**").group(name).build();
            }).toList();
    }

}
