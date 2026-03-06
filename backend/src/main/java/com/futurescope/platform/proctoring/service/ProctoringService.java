package com.futurescope.platform.proctoring.service;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.repository.InterviewRepository;
import com.futurescope.platform.proctoring.domain.ProctoringEvent;
import com.futurescope.platform.proctoring.domain.ProctoringSession;
import com.futurescope.platform.proctoring.repository.ProctoringEventRepository;
import com.futurescope.platform.proctoring.repository.ProctoringSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProctoringService {

    private final InterviewRepository interviewRepository;
    private final CandidateRepository candidateRepository;
    private final ProctoringSessionRepository sessionRepository;
    private final ProctoringEventRepository eventRepository;

    public ProctoringService(
            InterviewRepository interviewRepository,
            CandidateRepository candidateRepository,
            ProctoringSessionRepository sessionRepository,
            ProctoringEventRepository eventRepository
    ) {
        this.interviewRepository = interviewRepository;
        this.candidateRepository = candidateRepository;
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public ProctoringSession startSession(UUID interviewId, User currentUser) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
        ensureCandidateOwnsInterview(interview, currentUser);
        if (sessionRepository.findByInterview(interview).isPresent()) {
            throw new IllegalArgumentException("Proctoring session already started for this interview");
        }
        OffsetDateTime now = OffsetDateTime.now();
        ProctoringSession session = new ProctoringSession();
        session.setId(UUID.randomUUID());
        session.setInterview(interview);
        session.setStartedAt(now);
        session.setCreatedAt(now);
        return sessionRepository.save(session);
    }

    @Transactional
    public ProctoringEvent postEvent(UUID sessionId, String eventType, String detailsJson, BigDecimal weight, User currentUser) {
        ProctoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Proctoring session not found"));
        if (session.getEndedAt() != null) {
            throw new IllegalArgumentException("Proctoring session already ended");
        }
        ensureCandidateOwnsInterview(session.getInterview(), currentUser);
        OffsetDateTime now = OffsetDateTime.now();
        ProctoringEvent event = new ProctoringEvent();
        event.setId(UUID.randomUUID());
        event.setProctoringSession(session);
        event.setEventType(eventType);
        event.setOccurredAt(now);
        event.setDetailsJson(detailsJson);
        event.setWeight(weight != null ? weight : BigDecimal.ONE);
        return eventRepository.save(event);
    }

    @Transactional
    public ProctoringSession endSession(UUID sessionId, User currentUser) {
        ProctoringSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Proctoring session not found"));
        if (session.getEndedAt() != null) {
            throw new IllegalArgumentException("Proctoring session already ended");
        }
        ensureCandidateOwnsInterview(session.getInterview(), currentUser);
        OffsetDateTime now = OffsetDateTime.now();
        session.setEndedAt(now);
        List<ProctoringEvent> events = eventRepository.findByProctoringSessionOrderByOccurredAt(session);
        BigDecimal risk = computeRiskScore(events);
        session.setOverallRiskScore(risk);
        session.setSummaryJson(String.format("{\"eventCount\":%d,\"riskScore\":%s}", events.size(), risk));
        return sessionRepository.save(session);
    }

    private void ensureCandidateOwnsInterview(Interview interview, User currentUser) {
        Candidate candidate = candidateRepository.findByUser(currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Not a candidate"));
        JobApplication app = interview.getApplication();
        if (!app.getCandidate().getId().equals(candidate.getId())) {
            throw new IllegalArgumentException("Not authorized for this interview");
        }
    }

    private BigDecimal computeRiskScore(List<ProctoringEvent> events) {
        if (events.isEmpty()) return BigDecimal.ZERO;
        BigDecimal sum = events.stream()
                .map(e -> e.getWeight() != null ? e.getWeight() : BigDecimal.ONE)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal count = BigDecimal.valueOf(events.size());
        return sum.divide(count, 2, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100));
    }
}
