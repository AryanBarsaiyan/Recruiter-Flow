package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.PasswordReset;
import com.futurescope.platform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {

    Optional<PasswordReset> findTopByUserOrderByCreatedAtDesc(User user);

    Optional<PasswordReset> findByToken(String token);

}

