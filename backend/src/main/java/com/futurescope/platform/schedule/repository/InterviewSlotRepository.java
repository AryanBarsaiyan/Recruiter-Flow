package com.futurescope.platform.schedule.repository;

import com.futurescope.platform.schedule.domain.InterviewInvitation;
import com.futurescope.platform.schedule.domain.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, UUID> {

    List<InterviewSlot> findByInvitation(InterviewInvitation invitation);
}
