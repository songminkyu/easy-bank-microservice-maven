package io.github.songminkyu.gatewayserver.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter.Mode.DENY;

import io.github.songminkyu.gatewayserver.excetion.security.SecurityProblemSupport;
import io.github.songminkyu.gatewayserver.security.AuthoritiesConstants;
import io.github.songminkyu.gatewayserver.security.oauth2.JwtGrantedAuthorityConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        Converter<Jwt, Mono<AbstractAuthenticationToken>> authenticationConverter,
        SecurityProblemSupport problemSupport) {
        http
            .securityMatcher(
                new NegatedServerWebExchangeMatcher(
                    new OrServerWebExchangeMatcher(
                        ServerWebExchangeMatchers.pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/webjars/**",
                            "/swagger-ui.html"),
                        ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**")
                    )
                ))
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .cors(withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("script-src 'self'"))
                .frameOptions(frameOptions -> frameOptions.mode(DENY)))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport))
            .authorizeExchange(authz ->
                authz
                    .pathMatchers("/*/actuator/**").permitAll()
                    .pathMatchers(HttpMethod.GET).permitAll()
                    .pathMatchers("/eazybank/account/**", "/account/**").hasAuthority(AuthoritiesConstants.ACCOUNT)
                    .pathMatchers("/eazybank/card/**", "/card/**").hasAuthority(AuthoritiesConstants.CARD)
                    .pathMatchers("/eazybank/loan/**", "/loan/**").hasAuthority(AuthoritiesConstants.LOAN))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter))
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport));
        return http.build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> authenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthorityConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}