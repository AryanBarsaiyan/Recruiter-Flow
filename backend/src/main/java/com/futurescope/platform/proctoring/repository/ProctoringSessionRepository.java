package com.futurescope.platform.proctoring.repository;

import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.proctoring.domain.ProctoringSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProctoringSessionRepository extends JpaRepository<ProctoringSession, UUID> {

    Optional<ProctoringSession> findByInterview(Interview interview);
}
