package com.boardwith.admin.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwith.admin.domain.entity.AppAdmin;
import com.boardwith.admin.domain.entity.Role;
import com.boardwith.admin.repository.AppAdminRepository;
import com.boardwith.admin.repository.RoleRepository;
import com.boardwith.admin.dto.request.AdminRegistrationRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AppAdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AppAdminRepository adminRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new admin and assign roles to them.
     * Only accessible by ADMIN / SUPER_ADMIN (see {@code SecurityConfig}).
     *
     * @param request username, password and role names to assign
     * @return 201 Created with the new admin's username and roles,
     *         409 Conflict if username already taken,
     *         400 Bad Request if any role name does not exist
     */
    @PostMapping("/register-admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        if (adminRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Username already taken"));
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.roles()) {
            Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            roles.add(role);
        }

        AppAdmin admin = new AppAdmin(
            request.username(),
            passwordEncoder.encode(request.password()),
            roles
        );
        adminRepository.save(admin);

        List<String> roleNames = roles.stream()
            .map(Role::getName)
            .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "message", "Admin registered",
                "username", admin.getUsername(),
                "roles", roleNames
            ));
    }
}
