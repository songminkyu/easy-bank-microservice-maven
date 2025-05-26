package io.github.susimsek.card.config;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
    info = @Info(
        title = "Card microservice REST API Documentation",
        description = "EazyBank Card microservice REST API Documentation",
        version = "v1",
        contact = @Contact(
            name = "Şuayb Şimşek",
            email = "suaybsimsek58@gmail.com",
            url = "https://www.susimsek.github.io"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0"
        )
    ),
    externalDocs = @ExternalDocumentation(
        description = "EazyBank Card microservice REST API Documentation",
        url = "https://www.susimsek.github.io/swagger-ui.html"
    )
)
@SecurityScheme(name = "auth", type = SecuritySchemeType.OAUTH2,
    flows = @OAuthFlows(clientCredentials = @OAuthFlow(tokenUrl = "${springdoc.oAuthFlow.tokenUrl}", scopes = {
        @OAuthScope(name = "openid", description = "openid"),
        @OAuthScope(name = "email", description = "email"),
        @OAuthScope(name = "profile", description = "profile")
    })))
public class OpenApiConfig {

}
