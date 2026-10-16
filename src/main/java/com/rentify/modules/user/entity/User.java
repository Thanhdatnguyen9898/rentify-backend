package com.rentify.modules.user.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.rentify.modules.user.enums.AuthProvider;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity 
@Table (name = "users")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String phoneNumber;
    private String email;
    private String username;
    private String passwordHash;
    private String fullName;
    private String avatarUrl;
    private String bio;
    private Boolean isActive = true;
    private Boolean isPhoneVerified = false;
    private BigDecimal walletBalance = BigDecimal.ZERO;
    @Version 
    private Long version;   
    private Instant createdAt;
    private Instant updatedAt;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider = AuthProvider.LOCAL;
    private String googleId;
}
