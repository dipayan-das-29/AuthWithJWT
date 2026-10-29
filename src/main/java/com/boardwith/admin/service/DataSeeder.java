package com.boardwith.admin.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.boardwith.admin.domain.entity.AppAdmin;
import com.boardwith.admin.domain.entity.Permission;
import com.boardwith.admin.domain.entity.Role;
import com.boardwith.admin.helper.BusinessIdGenerator;
import com.boardwith.admin.repository.AppAdminRepository;
import com.boardwith.admin.repository.PermissionRepository;
import com.boardwith.admin.repository.RoleRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppAdminRepository adminRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final List<String> PERMISSIONS = Arrays.asList(
        "admin_users:read", "admin_users:create", "admin_users:update", "admin_users:disable", "admin_users:reset_credentials",
        "roles:read", "roles:manage",
        "users:read", "users:suspend", "users:reactivate",
        "trips:read",
        "verifications:read", "verifications:review",
        "sos:read", "sos:resolve",
        "reviews:read", "reviews:moderate",
        "audit:read"
    );

    private static final Map<String, List<String>> ROLE_PERMISSIONS = Map.of(
        "ADMIN", Arrays.asList("users:read", "trips:read", "verifications:read", "sos:read", "reviews:read"),
        "SUPER_ADMIN", PERMISSIONS,
        "SUPPORT", Arrays.asList("users:read", "trips:read", "verifications:read", "sos:read", "reviews:read"),
        "VERIFIER", Arrays.asList("users:read", "verifications:read", "verifications:review"),
        "MODERATOR", Arrays.asList("users:read", "users:suspend", "users:reactivate", "reviews:read", "reviews:moderate"),
        "SAFETY_OFFICER", Arrays.asList("users:read", "trips:read", "sos:read", "sos:resolve")
    );

    public DataSeeder(AppAdminRepository adminRepository, RoleRepository roleRepository,
                      PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed permissions
        for (String p : PERMISSIONS) {
            if (permissionRepository.findByName(p).isEmpty()) {
                Permission permission = new Permission(p);
                permission.setBusinessId(BusinessIdGenerator.generate("Permission", p,
                        permissionRepository::existsByBusinessId));
                permissionRepository.save(permission);
            }
        }

        // Seed roles
        for (Map.Entry<String, List<String>> entry : ROLE_PERMISSIONS.entrySet()) {
            Role role = roleRepository.findByName(entry.getKey()).orElseGet(() -> {
                Role newRole = new Role(entry.getKey());
                newRole.setBusinessId(BusinessIdGenerator.generate("Role", entry.getKey(),
                        roleRepository::existsByBusinessId));
                return roleRepository.save(newRole);
            });

            if (role.getBusinessId() == null) {
                role.setBusinessId(BusinessIdGenerator.generate("Role", entry.getKey(),
                        roleRepository::existsByBusinessId));
            }

            Set<Permission> perms = new LinkedHashSet<>();
            for (String permissionName : entry.getValue()) {
                permissionRepository.findByName(permissionName).ifPresent(perms::add);
            }
            role.setPermissions(perms);
            roleRepository.save(role);
        }

        seedAdmin("admin", "admin123", "ADMIN");
        seedAdmin("superadmin", "superadmin123", "SUPER_ADMIN");
    }

    private void seedAdmin(String username, String password, String roleName) {
        if (!adminRepository.existsByUsername(username)) {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            AppAdmin admin = new AppAdmin(username, passwordEncoder.encode(password), role);
            // Constructor already sets businessId = "AppAdmin_" + username (e.g. AppAdmin_admin).
            adminRepository.save(admin);
        }
    }
}