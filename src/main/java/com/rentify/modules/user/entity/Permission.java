package com.rentify.modules.user.entity;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "permissions")
@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String apiPath;
    private String method;
    private String module;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    @JsonIgnore
    private Set<Role> roles;
    // @PrePersist
    // public void handleBeforeCreate() {
    //     this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
    //     this.createdAt = Instant.now();
    // }

    // @PreUpdate
    // public void handleBeforeUpdate() {
    //     this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    //     this.updatedAt = Instant.now();
    // }
}
