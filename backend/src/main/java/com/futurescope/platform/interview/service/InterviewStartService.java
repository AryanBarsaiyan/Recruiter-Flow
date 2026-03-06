package com.futurescope.platform.interview.service;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.domain.InterviewQuestion;
import com.futurescope.platform.interview.domain.QuestionBankQuestion;
import com.futurescope.platform.interview.repository.InterviewQuestionRepository;
import com.futurescope.platform.interview.repository.InterviewRepository;
import com.futurescope.platform.interview.repository.QuestionBankQuestionRepository;
import com.futurescope.platform.schedule.domain.InterviewInvitation;
import com.futurescope.platform.schedule.repository.InterviewInvitationRepository;
import com.futurescope.platform.schedule.repository.InterviewSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InterviewStartService {

    private final InterviewInvitationRepository invitationRepository;
    private final InterviewSlotRepository slotRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final QuestionBankQuestionRepository questionBankQuestionRepository;

    public InterviewStartService(
            InterviewInvitationRepository invitationRepository,
            InterviewSlotRepository slotRepository,
            InterviewRepository interviewRepository,
            InterviewQuestionRepository interviewQuestionRepository,
            QuestionBankQuestionRepository questionBankQuestionRepository
    ) {
        this.invitationRepository = invitationRepository;
        this.slotRepository = slotRepository;
        this.interviewRepository = interviewRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.questionBankQuestionRepository = questionBankQuestionRepository;
    }

    @Transactional
    public StartInterviewResult startInterview(String invitationToken) {
        InterviewInvitation inv = invitationRepository.findByToken(invitationToken)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        if (!"accepted".equals(inv.getStatus())) {
            throw new IllegalArgumentException("Invitation must be accepted (slot booked) before starting");
        }
        if (inv.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Invitation has expired");
        }
        if (slotRepository.findByInvitation(inv).stream()
                .noneMatch(s -> s.getBookedByCandidateAt() != null && s.getCancelledAt() == null)) {
            throw new IllegalArgumentException("No valid slot booked for this invitation");
        }

        JobApplication application = inv.getApplication();
        List<Interview> existing = interviewRepository.findByApplication(application);
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException("Interview already started for this application");
        }

        List<QuestionBankQuestion> pool = questionBankQuestionRepository.findByJobAndActiveTrue(application.getJob());
        if (pool.isEmpty()) {
            pool = questionBankQuestionRepository.findByCompanyAndActiveTrue(application.getJob().getCompany());
        }
        if (pool.size() < 2) {
            throw new IllegalArgumentException("At least 2 questions must be configured for this job or company");
        }
        List<QuestionBankQuestion> selected = pool.stream().limit(2).toList();

        OffsetDateTime now = OffsetDateTime.now();
        Interview interview = new Interview();
        interview.setId(UUID.randomUUID());
        interview.setApplication(application);
        interview.setType(inv.getInterviewType());
        interview.setStatus("in_progress");
        interview.setStartedAt(now);
        interview.setCreatedAt(now);
        interviewRepository.save(interview);

        for (int i = 0; i < selected.size(); i++) {
            InterviewQuestion iq = new InterviewQuestion();
            iq.setId(UUID.randomUUID());
            iq.setInterview(interview);
            iq.setQuestion(selected.get(i));
            iq.setSequenceNumber(i + 1);
            iq.setAssignedAt(now);
            interviewQuestionRepository.save(iq);
        }

        InterviewQuestion first = interviewQuestionRepository.findByInterviewOrderBySequenceNumber(interview).get(0);
        return new StartInterviewResult(
                interview.getId(),
                first.getId(),
                first.getQuestion().getTitle(),
                first.getQuestion().getDescription(),
                first.getQuestion().getStarterCode()
        );
    }

    public record StartInterviewResult(
            UUID interviewId,
            UUID firstQuestionId,
            String firstQuestionTitle,
            String firstQuestionDescription,
            String firstQuestionStarterCode
    ) {}
}
