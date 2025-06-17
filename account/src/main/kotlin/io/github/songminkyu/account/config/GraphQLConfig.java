package io.github.songminkyu.account.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class GraphQLConfig {

    @Bean
    public GraphQLScalarType dateTimeScalarType() {
        return ExtendedScalars.DateTime;
    }

    @Bean
    public RuntimeWiringConfigurer graphqlConfigurer(
        List<GraphQLScalarType> graphQLScalarTypes
    ) {
        return builder -> graphQLScalarTypes.forEach(
            builder::scalar);
    }

    @Bean
    public HttpGraphQlClient.Builder<?> httpGraphQlClientBuilder(
        WebClient.Builder  loadBalancedWebClientBuilder) {
        return HttpGraphQlClient.builder(loadBalancedWebClientBuilder);
    }
}
