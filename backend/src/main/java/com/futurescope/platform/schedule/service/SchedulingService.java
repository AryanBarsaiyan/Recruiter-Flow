package com.futurescope.platform.schedule.service;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import com.futurescope.platform.schedule.domain.InterviewInvitation;
import com.futurescope.platform.schedule.domain.InterviewSlot;
import com.futurescope.platform.schedule.repository.InterviewInvitationRepository;
import com.futurescope.platform.schedule.repository.InterviewSlotRepository;
import com.futurescope.platform.schedule.web.dto.BookSlotRequest;
import com.futurescope.platform.schedule.web.dto.InvitationInfoResponse;
import com.futurescope.platform.schedule.web.dto.SlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SchedulingService {

    private final InterviewInvitationRepository interviewInvitationRepository;
    private final InterviewSlotRepository interviewSlotRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public SchedulingService(
            InterviewInvitationRepository interviewInvitationRepository,
            InterviewSlotRepository interviewSlotRepository,
            JobApplicationRepository jobApplicationRepository
    ) {
        this.interviewInvitationRepository = interviewInvitationRepository;
        this.interviewSlotRepository = interviewSlotRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Transactional(readOnly = true)
    public InvitationInfoResponse getInvitationByToken(String token) {
        InterviewInvitation inv = interviewInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!"pending".equals(inv.getStatus()) && !"accepted".equals(inv.getStatus())) {
            throw new IllegalArgumentException("Invitation is no longer valid");
        }
        if (inv.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }
        return toInvitationInfo(inv);
    }

    @Transactional
    public SlotResponse bookSlot(String token, BookSlotRequest request) {
        InterviewInvitation inv = interviewInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!"pending".equals(inv.getStatus())) {
            throw new IllegalArgumentException("Invitation already used or cancelled");
        }
        if (inv.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }

        InterviewSlot slot = new InterviewSlot();
        slot.setId(UUID.randomUUID());
        slot.setInvitation(inv);
        slot.setScheduledStartAt(request.getScheduledStartAt());
        slot.setScheduledEndAt(request.getScheduledEndAt());
        OffsetDateTime now = OffsetDateTime.now();
        slot.setBookedByCandidateAt(now);
        slot.setRescheduleCount(0);
        slot.setNoShowCandidate(false);
        slot.setNoShowRecruiter(false);
        interviewSlotRepository.save(slot);

        inv.setStatus("accepted");
        interviewInvitationRepository.save(inv);

        JobApplication app = inv.getApplication();
        app.setStatus("scheduled");
        app.setLastStatusAt(now);
        jobApplicationRepository.save(app);

        SlotResponse response = new SlotResponse();
        response.setId(slot.getId());
        response.setScheduledStartAt(slot.getScheduledStartAt());
        response.setScheduledEndAt(slot.getScheduledEndAt());
        response.setBookedByCandidateAt(slot.getBookedByCandidateAt());
        return response;
    }

    private InvitationInfoResponse toInvitationInfo(InterviewInvitation inv) {
        JobApplication app = inv.getApplication();
        InvitationInfoResponse r = new InvitationInfoResponse();
        r.setInvitationId(inv.getId());
        r.setApplicationId(app.getId());
        r.setJobId(app.getJob().getId());
        r.setJobTitle(app.getJob().getTitle());
        r.setInterviewType(inv.getInterviewType());
        r.setExpiresAt(inv.getExpiresAt());
        r.setStatus(inv.getStatus());
        return r;
    }
}
