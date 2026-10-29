package com.boardwith.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwith.admin.domain.entity.Permission;
import com.boardwith.admin.repository.PermissionRepository;
import com.boardwith.admin.domain.entity.Role;
import com.boardwith.admin.repository.RoleRepository;

@RestController
@RequestMapping("/api/admin")
public class RolePermissionController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionController(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping("/roles")
    public List<Role> roles() {
        return roleRepository.findAll();
    }

    @GetMapping("/permissions")
    public List<Permission> permissions() {
        return permissionRepository.findAll();
    }
}
