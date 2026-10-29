package com.boardwith.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    @GetMapping("/public/hello")
    public Map<String, String> publicHello() {
        return Map.of("message", "Hello from a public endpoint");
    }

    @GetMapping("/user/me")
    public Map<String, Object> me(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        return Map.of("username", authentication.getName(), "roles", roles);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/admin/dashboard")
    public Map<String, String> adminDashboard() {
        return Map.of("message", "Welcome to the admin dashboard");
    }
}
