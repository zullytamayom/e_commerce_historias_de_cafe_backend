package com.historias_de_cafe.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
            "app", "Historias de Café API",
            "status", "Online",
            "documentation", "/swagger-ui.html"
        );
    }
}