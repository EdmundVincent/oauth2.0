package com.example.sbapp.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("scope") String scope,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("id_token") String idToken
) {
}
