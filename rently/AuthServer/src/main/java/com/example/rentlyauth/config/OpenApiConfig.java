package com.example.rentlyauth.config;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Auth Server API", version = "v1",description = "API do rejestracji, autoryzacji i zarządzania hasłem użytkownika"),
        security = @SecurityRequirement(name = "oidc")
)
@SecurityScheme(
        name = "oidc",
        type = SecuritySchemeType.OPENIDCONNECT,
        openIdConnectUrl = "${spring.security.oauth2.authorizationserver.issuer-uri}/.well-known/openid-configuration"
)
class OpenApiConfig {}
