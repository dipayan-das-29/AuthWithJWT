package com.boardwith.admin.domain.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String businessId;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    protected Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}