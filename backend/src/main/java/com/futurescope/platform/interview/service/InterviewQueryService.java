package com.futurescope.platform.interview.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.domain.InterviewQuestion;
import com.futurescope.platform.interview.repository.InterviewQuestionRepository;
import com.futurescope.platform.interview.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InterviewQueryService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final RbacService rbacService;

    public InterviewQueryService(InterviewRepository interviewRepository,
                                InterviewQuestionRepository interviewQuestionRepository,
                                RbacService rbacService) {
        this.interviewRepository = interviewRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.rbacService = rbacService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getById(UUID interviewId, User currentUser) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
        if ("candidate".equals(currentUser.getUserType())) {
            if (!interview.getApplication().getCandidate().getUser().getId().equals(currentUser.getId())) {
                throw new IllegalArgumentException("Not authorized to view this interview");
            }
        } else {
            rbacService.requireActiveCompanyMember(currentUser, interview.getApplication().getJob().getCompany().getId());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", interview.getId());
        out.put("applicationId", interview.getApplication().getId());
        out.put("type", interview.getType());
        out.put("status", interview.getStatus());
        out.put("startedAt", interview.getStartedAt());
        out.put("endedAt", interview.getEndedAt());
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> submitCode(UUID interviewId, UUID interviewQuestionId, User currentUser, String language, String code) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
        if (!interview.getApplication().getCandidate().getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }
        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewOrderBySequenceNumber(interview);
        boolean found = questions.stream().anyMatch(q -> q.getId().equals(interviewQuestionId));
        if (!found) throw new IllegalArgumentException("Interview question not found");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "received");
        out.put("interviewQuestionId", interviewQuestionId);
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> answerFollowup(UUID interviewId, UUID followupQuestionId, User currentUser, String answerText) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));
        if (!interview.getApplication().getCandidate().getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Not authorized");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "received");
        out.put("followupQuestionId", followupQuestionId);
        return out;
    }
}
