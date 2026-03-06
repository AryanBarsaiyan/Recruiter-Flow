package com.futurescope.platform.schedule.repository;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.schedule.domain.InterviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InterviewInvitationRepository extends JpaRepository<InterviewInvitation, UUID> {

    Optional<InterviewInvitation> findByToken(String token);

    @Query("select i from InterviewInvitation i where i.application.id = :applicationId")
    Optional<InterviewInvitation> findByApplicationId(@Param("applicationId") UUID applicationId);
}
