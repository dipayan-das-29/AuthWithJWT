package com.boardwith.admin.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.boardwith.admin.domain.entity.AppAdmin;

public interface AppAdminRepository extends JpaRepository<AppAdmin, UUID> {

    Optional<AppAdmin> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByBusinessId(String businessId);
}