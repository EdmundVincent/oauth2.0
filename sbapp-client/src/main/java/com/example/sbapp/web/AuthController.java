package com.example.sbapp.web;

import com.example.sbapp.config.IdpProperties;
import com.example.sbapp.oauth.IdpClient;
import com.example.sbapp.oauth.Pkce;
import com.example.sbapp.oauth.TokenResponse;
import com.example.sbapp.session.SessionKeys;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final IdpProperties props;
    private final IdpClient idpClient;

    public AuthController(IdpProperties props, IdpClient idpClient) {
        this.props = props;
        this.idpClient = idpClient;
    }

    @GetMapping("/login")
    public String login(HttpSession session) {
        String state = Pkce.newState();
        String nonce = Pkce.newNonce();
        String verifier = Pkce.newVerifier();
        String challenge = Pkce.challengeS256(verifier);

        session.setAttribute(SessionKeys.OAUTH_STATE, state);
        session.setAttribute(SessionKeys.OIDC_NONCE, nonce);
        session.setAttribute(SessionKeys.PKCE_VERIFIER, verifier);

        String stateMasked = state.length() > 8 ? state.substring(0, 4) + "****" + state.substring(state.length() - 4) : "****";
        String nonceMasked = nonce.length() > 8 ? nonce.substring(0, 4) + "****" + nonce.substring(nonce.length() - 4) : "****";
        String challMasked = challenge.length() > 8 ? challenge.substring(0, 4) + "****" + challenge.substring(challenge.length() - 4) : "****";

        URI authorizeUri = UriComponentsBuilder
                .fromHttpUrl(props.publicBaseUrl())
                .path("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", props.clientId())
                .queryParam("redirect_uri", props.redirectUri())
                .queryParam("scope", props.scope())
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .queryParam("code_challenge", challenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .encode()
                .toUri();

        log.info("authorize redirect prepared: client_id={}, redirect_uri={}, scope={}, code_challenge_method=S256, state={}, nonce={}",
                props.clientId(), props.redirectUri(), props.scope(), stateMasked, nonceMasked);
        log.debug("authorize code_challenge={}", challMasked);

        return "redirect:" + authorizeUri;
    }

    @GetMapping("/callback")
    public String callback(
            HttpSession session,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "error", required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription,
            Model model
    ) {
        if (error != null && !error.isBlank()) {
            model.addAttribute("title", "IdP authorization error");
            model.addAttribute("message", error + (errorDescription == null ? "" : (": " + errorDescription)));
            model.addAttribute("status", 400);
            log.warn("authorization error: {} {}", error, errorDescription == null ? "" : errorDescription);
            return "app-error";
        }

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing code");
        }

        String expectedState = (String) session.getAttribute(SessionKeys.OAUTH_STATE);
        if (expectedState == null || !expectedState.equals(state)) {
            log.warn("state mismatch: expected in session, got {}", state);
            model.addAttribute("title", "Invalid state");
            model.addAttribute("message", "state does not match session");
            model.addAttribute("status", 400);
            return "app-error";
        }

        log.info("callback received: code_length={}, state=ok", code.length());

        String verifier = (String) session.getAttribute(SessionKeys.PKCE_VERIFIER);
        if (verifier == null || verifier.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing pkce verifier in session");
        }

        TokenResponse token = idpClient.exchangeAuthorizationCode(code, verifier);
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "failed to exchange token");
        }

        log.info("token exchanged: token_type={}, expires_in={}", token.tokenType(), token.expiresIn());

        session.removeAttribute(SessionKeys.OAUTH_STATE);
        session.removeAttribute(SessionKeys.OIDC_NONCE);
        session.removeAttribute(SessionKeys.PKCE_VERIFIER);

        session.setAttribute(SessionKeys.ACCESS_TOKEN, token.accessToken());
        session.setAttribute(SessionKeys.TOKEN_TYPE, token.tokenType());
        if (token.idToken() != null && !token.idToken().isBlank()) {
            session.setAttribute(SessionKeys.ID_TOKEN, token.idToken());
        }
        if (token.refreshToken() != null && !token.refreshToken().isBlank()) {
            session.setAttribute(SessionKeys.REFRESH_TOKEN, token.refreshToken());
        }

        Instant expiresAt = idpClient.expiresAt(token);
        if (expiresAt != null) {
            session.setAttribute(SessionKeys.EXPIRES_AT_EPOCH_SECONDS, expiresAt.getEpochSecond());
        }

        return "redirect:/me";
    }
}
