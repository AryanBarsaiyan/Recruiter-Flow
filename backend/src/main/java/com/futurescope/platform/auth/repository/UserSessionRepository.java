package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.domain.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    List<UserSession> findByUserAndActiveIsTrue(User user);

    Optional<UserSession> findBySessionToken(String sessionToken);

    @Modifying
    @Query("update UserSession s set s.active = false where s.user = :user and s.active = true")
    int deactivateAllActiveSessions(@Param("user") User user);

}

