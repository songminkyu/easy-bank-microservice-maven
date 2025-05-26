package io.github.susimsek.account.config;

import io.github.susimsek.account.logging.restclient.LoggingInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class RestClientConfig {

    @Bean
    @ConditionalOnClass(RestClient.class)
    @LoadBalanced
    public RestClient.Builder loadBalancedWebClientBuilder(
        LoggingInterceptor loggingInterceptor,
        ObjectProvider<RestClientCustomizer> customizerProvider) {
        var builder = RestClient.builder()
            .requestInterceptor(loggingInterceptor);
        customizerProvider.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder;
    }
}
