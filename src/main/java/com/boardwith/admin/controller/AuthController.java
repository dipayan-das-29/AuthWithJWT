package com.boardwith.admin.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwith.admin.domain.entity.AppAdmin;
import com.boardwith.admin.repository.AppAdminRepository;
import com.boardwith.admin.service.AppUserDetailsService;
import com.boardwith.admin.repository.RoleRepository;
import com.boardwith.admin.config.JwtService;
import com.boardwith.admin.dto.request.LoginRequest;
import com.boardwith.admin.dto.request.RegistrationRequest;
import com.boardwith.admin.dto.response.AuthResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class AuthController {

    private final AppAdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService userDetailsService;

    public AuthController(AppAdminRepository adminRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          AppUserDetailsService userDetailsService) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegistrationRequest request) {
        if (adminRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Username already taken"));
        }

        // The USER role has been removed. New registrations are created without any role;
        // an administrator must assign roles explicitly via /api/admin/register-admin.
        adminRepository.save(new AppAdmin(request.username(), passwordEncoder.encode(request.password()), java.util.Set.of()));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of("message", "User registered"));
    }

    /**
     * Authenticate with username/password and receive a JWT access token.
     *
     * @return 200 with {@link AuthResponse} containing the JWT token,
     *         401 if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
