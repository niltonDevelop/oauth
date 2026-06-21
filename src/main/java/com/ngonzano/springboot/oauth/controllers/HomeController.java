package com.ngonzano.springboot.oauth.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home(Authentication authentication) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Login exitoso en el servidor OAuth");
        payload.put("username", authentication.getName());
        payload.put("authorities", authentication.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toList()));
        return payload;
    }
}
