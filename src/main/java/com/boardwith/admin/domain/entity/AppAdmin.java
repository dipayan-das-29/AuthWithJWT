package com.boardwith.admin.domain.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import com.boardwith.admin.helper.BusinessIdGenerator;

@Entity
@Table(name = "app_admins")
public class AppAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String businessId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admin_roles",
        joinColumns = @JoinColumn(name = "admin_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled = true;

    protected AppAdmin() {
    }

    public AppAdmin(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.businessId = buildBusinessId(username);
        this.roles.add(role);
    }

    public AppAdmin(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.businessId = buildBusinessId(username);
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    private static String buildBusinessId(String username) {
        // username is unique, so "AppAdmin_" + username is unique without a DB lookup.
        return BusinessIdGenerator.generate("AppAdmin", username, null);
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}