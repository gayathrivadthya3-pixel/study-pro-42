package com.studypulse.repository;

import com.studypulse.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByMobileNumberAndVerifiedFalseOrderByCreatedAtDesc(String mobileNumber);
}
