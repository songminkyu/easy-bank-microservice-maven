package io.github.songminkyu.card.security.websocket;

import static reactor.core.publisher.Mono.empty;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.server.WebSocketGraphQlInterceptor;
import org.springframework.graphql.server.WebSocketGraphQlRequest;
import org.springframework.graphql.server.WebSocketSessionInfo;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class GraphQLWsAuthenticationInterceptor implements WebSocketGraphQlInterceptor {

    private static final String AUTHORIZATION_CONNECTION_INIT_PAYLOAD_KEY_NAME = "Authorization";
    private static final String AUTHENTICATION_SESSION_ATTRIBUTE_KEY =
        GraphQLWsAuthenticationInterceptor.class.getCanonicalName() + ".authentication";

    private final AuthenticationProvider authenticationProvider;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, WebGraphQlInterceptor.Chain chain) {
        if (!(request instanceof WebSocketGraphQlRequest wsRequest)) {
            return chain.next(request);
        }
        var optionalAuthentication = Optional.of(wsRequest)
            .map(webSocketGraphQlRequest -> getAuthentication(webSocketGraphQlRequest.getSessionInfo()))
            .flatMap(this::authenticate);
        optionalAuthentication.ifPresent(authentication ->
            SecurityContextHolder.getContext().setAuthentication(authentication));
        return chain.next(request);
    }

    @Override
    public Mono<Void> handleCancelledSubscription(WebSocketSessionInfo sessionInfo, String subscriptionId) {
        clearAuthentication(sessionInfo);
        return empty();
    }

    @Override
    public void handleConnectionClosed(WebSocketSessionInfo sessionInfo, int statusCode,
                                       Map<String, Object> connectionInitPayload) {
        clearAuthentication(sessionInfo);
    }

    @Override
    public Mono<Object> handleConnectionInitialization(WebSocketSessionInfo sessionInfo,
                                                       Map<String, Object> connectionInitPayload) {
        String accessToken = resolveToken(connectionInitPayload);
        if (StringUtils.hasText(accessToken)) {
            setAuthentication(sessionInfo, new BearerTokenAuthenticationToken(accessToken));
        }
        return empty();
    }

    private String resolveToken(Map<String, Object> connectionInitPayload) {
        return resolveTokenFromPayload(connectionInitPayload);
    }

    private String resolveTokenFromPayload(Map<String, Object> connectionInitPayload) {
        String tokenFromPayload = (String) connectionInitPayload.get(AUTHORIZATION_CONNECTION_INIT_PAYLOAD_KEY_NAME);
        if (tokenFromPayload != null
            && tokenFromPayload.startsWith(OAuth2AccessToken.TokenType.BEARER.getValue())) {
            return tokenFromPayload.substring(OAuth2AccessToken.TokenType.BEARER.getValue().length());
        }
        return null;
    }


    private BearerTokenAuthenticationToken getAuthentication(WebSocketSessionInfo webSocketSessionInfo) {
        return (BearerTokenAuthenticationToken) webSocketSessionInfo.getAttributes()
            .get(AUTHENTICATION_SESSION_ATTRIBUTE_KEY);
    }

    private void setAuthentication(WebSocketSessionInfo webSocketSessionInfo,
                                   BearerTokenAuthenticationToken authentication) {
        webSocketSessionInfo.getAttributes().put(AUTHENTICATION_SESSION_ATTRIBUTE_KEY, authentication);
    }

    private void clearAuthentication(WebSocketSessionInfo webSocketSessionInfo) {
        webSocketSessionInfo.getAttributes().remove(AUTHENTICATION_SESSION_ATTRIBUTE_KEY);
    }

    private Optional<Authentication> authenticate(Authentication authentication) {
        try {
            return Optional.of(authenticationProvider.authenticate(authentication));
        } catch (AuthenticationException ex) {
            return Optional.empty();
        }
    }
}


