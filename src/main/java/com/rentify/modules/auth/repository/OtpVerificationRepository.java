package com.rentify.modules.auth.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.rentify.modules.auth.entity.OtpVerification;
import com.rentify.modules.auth.enums.OtpType;

@Repository 
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long>,
    JpaSpecificationExecutor<OtpVerification> {
        Optional<OtpVerification>  findTopByPhoneNumberAndTypeAndIsUsedFalseOrderByCreatedAtDesc(String phone, OtpType type);
    }