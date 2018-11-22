package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GreetingController {
    @GetMapping("/sse-emitter/web")
    public String sseTest(String name, Model model) {
        return "index.html";
    }

    @GetMapping("/webflux/web")
    public String webFluxText(String name, Model model) {
        return "webflux.html";
    }
}