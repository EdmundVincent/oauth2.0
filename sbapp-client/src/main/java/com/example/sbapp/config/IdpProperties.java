package com.example.sbapp.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "idp")
public record IdpProperties(
        @NotBlank String publicBaseUrl,
        @NotBlank String internalBaseUrl,
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String redirectUri,
        @NotBlank String scope,
        @NotBlank String postLogoutRedirectUri
) {
}
