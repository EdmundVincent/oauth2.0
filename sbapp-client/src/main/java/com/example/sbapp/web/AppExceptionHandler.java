package com.example.sbapp.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice(annotations = Controller.class)
public class AppExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, Model model) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        model.addAttribute("title", "Request failed");
        model.addAttribute("message", ex.getReason() == null ? ex.getMessage() : ex.getReason());
        model.addAttribute("status", status == null ? ex.getStatusCode().value() : status.value());
        return "app-error";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("title", "Unexpected error");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("status", 500);
        return "app-error";
    }
}
