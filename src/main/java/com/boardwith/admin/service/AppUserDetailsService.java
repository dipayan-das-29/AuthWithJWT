package com.boardwith.admin.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.boardwith.admin.domain.entity.AppAdmin;
import com.boardwith.admin.domain.entity.Permission;
import com.boardwith.admin.domain.entity.Role;
import com.boardwith.admin.repository.AppAdminRepository;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppAdminRepository adminRepository;

    public AppUserDetailsService(AppAdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppAdmin appAdmin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        for (Role role : appAdmin.getRoles()) {
            roles.add(role.getName());
            for (Permission permission : role.getPermissions()) {
                permissions.add(permission.getName());
            }
        }

        Set<GrantedAuthority> authorities = new HashSet<>();
        // Roles get the "ROLE_" prefix so Spring Security hasRole()/hasAnyRole() works.
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        // Permissions are stored as-is (e.g. "admin_users:read") for method-security checks.
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }

        return User.builder()
            .username(appAdmin.getUsername())
            .password(appAdmin.getPassword())
            .authorities(authorities)
            .disabled(!appAdmin.isEnabled())
            .build();
    }
}