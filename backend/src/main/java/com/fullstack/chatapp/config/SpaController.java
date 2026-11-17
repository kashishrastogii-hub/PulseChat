package com.fullstack.chatapp.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Lets the bundled React app handle direct navigation to its client-side routes. */
@Controller
public class SpaController {
    @GetMapping({"/", "/login", "/register", "/chat", "/not-found"})
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
