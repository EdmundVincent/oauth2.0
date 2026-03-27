package com.example.sbapp.web;

import com.example.sbapp.config.IdpProperties;
import com.example.sbapp.oauth.Pkce;
import com.example.sbapp.session.SessionKeys;
import jakarta.servlet.http.HttpSession;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class DebugController {
    private final IdpProperties props;

    public DebugController(IdpProperties props) {
        this.props = props;
    }

    @GetMapping("/debug/authorize-preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> authorizePreview(HttpSession session) {
        String state = Pkce.newState();
        String nonce = Pkce.newNonce();
        String verifier = Pkce.newVerifier();
        String challenge = Pkce.challengeS256(verifier);
        session.setAttribute(SessionKeys.OAUTH_STATE, state);
        session.setAttribute(SessionKeys.OIDC_NONCE, nonce);
        session.setAttribute(SessionKeys.PKCE_VERIFIER, verifier);

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

        Map<String, Object> out = new HashMap<>();
        out.put("authorize_url", authorizeUri.toString());
        out.put("client_id", props.clientId());
        out.put("redirect_uri", props.redirectUri());
        out.put("scope", props.scope());
        out.put("state_masked", state.length() > 8 ? state.substring(0, 4) + "****" + state.substring(state.length() - 4) : "****");
        out.put("nonce_masked", nonce.length() > 8 ? nonce.substring(0, 4) + "****" + nonce.substring(nonce.length() - 4) : "****");
        out.put("code_challenge_method", "S256");
        out.put("code_challenge_masked", challenge.length() > 8 ? challenge.substring(0, 4) + "****" + challenge.substring(challenge.length() - 4) : "****");
        return ResponseEntity.ok(out);
    }

    @GetMapping("/debug/session-brief")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sessionBrief(HttpSession session) {
        Map<String, Object> out = new HashMap<>();
        Object at = session.getAttribute(SessionKeys.ACCESS_TOKEN);
        Object rt = session.getAttribute(SessionKeys.REFRESH_TOKEN);
        Object tp = session.getAttribute(SessionKeys.TOKEN_TYPE);
        Object exp = session.getAttribute(SessionKeys.EXPIRES_AT_EPOCH_SECONDS);
        out.put("logged_in", at != null);
        out.put("token_type", tp == null ? "" : tp.toString());
        out.put("has_refresh_token", rt != null);
        if (exp instanceof Number n) {
            out.put("expires_at_epoch_seconds", n.longValue());
            out.put("expires_in_seconds", Math.max(0, n.longValue() - Instant.now().getEpochSecond()));
        } else {
            out.put("expires_at_epoch_seconds", null);
            out.put("expires_in_seconds", null);
        }
        return ResponseEntity.ok(out);
    }
}
