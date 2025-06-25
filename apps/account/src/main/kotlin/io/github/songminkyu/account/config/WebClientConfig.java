package io.github.songminkyu.account.config;

import io.github.songminkyu.account.logging.webclient.LoggingExchangeFilterFunction;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({WebClientProperties.class})
public class WebClientConfig {

    private static final String CUSTOMIZER_NAME = "oauth2AuthorizedClientExchangeFunction";

    @Bean
    public HttpClient httpClient(WebClientProperties webClientProperties) {
        var config = webClientProperties.getHttpClient().getConfig();
        return HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(config.getReadTimeout() / 1000))
                .addHandlerLast(new WriteTimeoutHandler(config.getWriteTimeout() / 1000)));
    }

    @Bean
    @ConditionalOnClass(WebClient.class)
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder(
        HttpClient httpClient,
        LoggingExchangeFilterFunction loggingExchangeFilterFunction,
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2AuthorizedClientExchangeFunction,
        ObjectProvider<WebClientCustomizer> customizerProvider) {
        var builder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .filter(oauth2AuthorizedClientExchangeFunction)
            .filter(loggingExchangeFilterFunction);
        customizerProvider.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder;
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var authorizedClientManager =
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
    @ConditionalOnClass(ExchangeFunction.class)
    public ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2AuthorizedClientExchangeFunction(
        OAuth2AuthorizedClientManager authorizedClientManager,
        WebClientProperties webClientProperties) {
        var config = webClientProperties.getOauth2();
        var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId(config.getClientRegistrationId());
        return oauth2Client;
    }
}
