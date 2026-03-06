package com.futurescope.platform.proctoring.repository;

import com.futurescope.platform.proctoring.domain.ProctoringEvent;
import com.futurescope.platform.proctoring.domain.ProctoringSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProctoringEventRepository extends JpaRepository<ProctoringEvent, UUID> {

    List<ProctoringEvent> findByProctoringSessionOrderByOccurredAt(ProctoringSession session);
}
