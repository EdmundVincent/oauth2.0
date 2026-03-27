package com.example.sbapp.web;

import com.example.sbapp.oauth.IdpClient;
import com.example.sbapp.session.SessionKeys;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MeController {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());
    private static final Logger log = LoggerFactory.getLogger(MeController.class);

    private final IdpClient idpClient;
    private final ObjectMapper objectMapper;

    public MeController(IdpClient idpClient, ObjectMapper objectMapper) {
        this.idpClient = idpClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/me")
    public String me(HttpSession session, Model model) throws Exception {
        String accessToken = (String) session.getAttribute(SessionKeys.ACCESS_TOKEN);
        Object expObj = session.getAttribute(SessionKeys.EXPIRES_AT_EPOCH_SECONDS);
        String refreshToken = (String) session.getAttribute(SessionKeys.REFRESH_TOKEN);

        if (refreshToken != null && accessToken != null && expObj != null) {
            long expEpoch = (expObj instanceof Number n) ? n.longValue() : Long.parseLong(expObj.toString());
            Instant exp = Instant.ofEpochSecond(expEpoch);
            if (Instant.now().isAfter(exp.minusSeconds(15))) {
                log.info("access token nearing expiry, attempting refresh");
                var token = idpClient.refresh(refreshToken);
                if (token != null && token.accessToken() != null && !token.accessToken().isBlank()) {
                    session.setAttribute(SessionKeys.ACCESS_TOKEN, token.accessToken());
                    session.setAttribute(SessionKeys.TOKEN_TYPE, token.tokenType());
                    if (token.refreshToken() != null && !token.refreshToken().isBlank()) {
                        session.setAttribute(SessionKeys.REFRESH_TOKEN, token.refreshToken());
                    }
                    Instant newExp = idpClient.expiresAt(token);
                    if (newExp != null) {
                        session.setAttribute(SessionKeys.EXPIRES_AT_EPOCH_SECONDS, newExp.getEpochSecond());
                    }
                    accessToken = token.accessToken();
                    log.info("token refresh succeeded, new expiry set");
                } else {
                    log.warn("token refresh failed, clearing session");
                    session.invalidate();
                    return "redirect:/";
                }
            }
        }

        if (accessToken == null || accessToken.isBlank()) {
            return "redirect:/";
        }

        log.info("calling userinfo endpoint");
        JsonNode userInfo = idpClient.userInfo(accessToken);

        model.addAttribute("userinfoJson", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userInfo));
        model.addAttribute("tokenType", session.getAttribute(SessionKeys.TOKEN_TYPE));
        model.addAttribute("idToken", session.getAttribute(SessionKeys.ID_TOKEN));

        Object exp = session.getAttribute(SessionKeys.EXPIRES_AT_EPOCH_SECONDS);
        if (exp instanceof Number num) {
            model.addAttribute("expiresAt", formatter.format(Instant.ofEpochSecond(num.longValue())));
        } else {
            model.addAttribute("expiresAt", "");
        }

        return "me";
    }
}
