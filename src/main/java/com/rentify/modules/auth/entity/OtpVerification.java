package com.rentify.modules.auth.entity;

import java.time.Instant;

import com.rentify.common.util.SecurityUtil;
import com.rentify.modules.auth.enums.OtpType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table (name = "otp_verifications")
@Getter
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class OtpVerification {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String phoneNumber;
    private String otpCode;
    @Enumerated(EnumType.STRING)
    private OtpType  type ;
    private Instant expiresAt;
    private Instant createdAt;
    private boolean isUsed;
    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }
}
