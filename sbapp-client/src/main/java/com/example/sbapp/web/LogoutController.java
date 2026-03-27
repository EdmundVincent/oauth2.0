package com.example.sbapp.web;

import com.example.sbapp.config.IdpProperties;
import com.example.sbapp.oauth.Pkce;
import com.example.sbapp.session.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class LogoutController {
    private static final Logger log = LoggerFactory.getLogger(LogoutController.class);
    private final IdpProperties props;

    public LogoutController(IdpProperties props) {
        this.props = props;
    }

    @GetMapping("/logout")
    public String federatedLogout(HttpSession session) {
        session.invalidate();

        String state = Pkce.newState();
        UriComponentsBuilder b = UriComponentsBuilder
                .fromHttpUrl(props.publicBaseUrl())
                .path("/connect/logout")
                .queryParam("client_id", props.clientId())
                .queryParam("post_logout_redirect_uri", props.postLogoutRedirectUri())
                .queryParam("state", state);
        log.info("redirecting to IdP end_session: client_id={}, post_logout_redirect_uri={}", props.clientId(), props.postLogoutRedirectUri());
        return "redirect:" + b.build().encode().toUriString();
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        log.info("local session invalidated");
        return "redirect:/";
    }
}
