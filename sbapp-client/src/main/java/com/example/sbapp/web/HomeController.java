package com.example.sbapp.web;

import com.example.sbapp.config.IdpProperties;
import com.example.sbapp.session.SessionKeys;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final IdpProperties props;

    public HomeController(IdpProperties props) {
        this.props = props;
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        Object accessToken = session.getAttribute(SessionKeys.ACCESS_TOKEN);
        model.addAttribute("idpRegisterUrl", props.publicBaseUrl() + "/register");
        model.addAttribute("loggedIn", accessToken != null);
        return "index";
    }
}
