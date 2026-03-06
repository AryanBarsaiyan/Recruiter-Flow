package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.EmailVerification;
import com.futurescope.platform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

    Optional<EmailVerification> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<EmailVerification> findByToken(String token);

}

