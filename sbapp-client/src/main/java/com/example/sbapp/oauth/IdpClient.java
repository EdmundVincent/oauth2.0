package com.example.sbapp.oauth;

import com.example.sbapp.config.IdpProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class IdpClient {
    private final RestClient restClient;
    private final IdpProperties props;

    public IdpClient(RestClient restClient, IdpProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public TokenResponse exchangeAuthorizationCode(String code, String codeVerifier) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", props.redirectUri());
        form.add("code_verifier", codeVerifier);

        return restClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(props.clientId(), props.clientSecret()))
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }

    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        return restClient
                .post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .headers(h -> h.setBasicAuth(props.clientId(), props.clientSecret()))
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
    }

    public JsonNode userInfo(String accessToken) {
        return restClient
                .get()
                .uri("/oauth2/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);
    }

    public Instant expiresAt(TokenResponse token) {
        if (token == null || token.expiresIn() == null) {
            return null;
        }
        return Instant.now().plusSeconds(token.expiresIn());
    }
}
