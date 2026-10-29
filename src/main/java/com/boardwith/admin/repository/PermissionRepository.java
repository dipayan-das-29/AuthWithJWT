package com.boardwith.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boardwith.admin.domain.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    Optional<Permission> findByName(String name);

    boolean existsByBusinessId(String businessId);
}