package com.futurescope.platform.interview.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.interview.service.InterviewQueryService;
import com.futurescope.platform.interview.service.InterviewStartService;
import com.futurescope.platform.interview.web.dto.StartInterviewRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/interviews")
public class InterviewController {

    private final InterviewStartService interviewStartService;
    private final InterviewQueryService interviewQueryService;

    public InterviewController(InterviewStartService interviewStartService,
                               InterviewQueryService interviewQueryService) {
        this.interviewStartService = interviewStartService;
        this.interviewQueryService = interviewQueryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getInterview(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(interviewQueryService.getById(id, currentUser));
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> start(@Valid @RequestBody StartInterviewRequest request) {
        InterviewStartService.StartInterviewResult result = interviewStartService.startInterview(request.getInvitationToken());
        return ResponseEntity.ok(Map.of(
                "interviewId", result.interviewId(),
                "firstQuestionId", result.firstQuestionId(),
                "firstQuestionTitle", result.firstQuestionTitle(),
                "firstQuestionDescription", result.firstQuestionDescription(),
                "firstQuestionStarterCode", result.firstQuestionStarterCode() != null ? result.firstQuestionStarterCode() : ""
        ));
    }

    @PostMapping("/{interviewId}/questions/{interviewQuestionId}/submit-code")
    public ResponseEntity<Map<String, Object>> submitCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID interviewId,
            @PathVariable UUID interviewQuestionId,
            @RequestBody Map<String, String> body
    ) {
        Map<String, Object> result = interviewQueryService.submitCode(interviewId, interviewQuestionId, currentUser, body.get("language"), body.get("code"));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{interviewId}/followups/{followupQuestionId}/answer")
    public ResponseEntity<Map<String, Object>> answerFollowup(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID interviewId,
            @PathVariable UUID followupQuestionId,
            @RequestBody Map<String, String> body
    ) {
        Map<String, Object> result = interviewQueryService.answerFollowup(interviewId, followupQuestionId, currentUser, body.get("answerText"));
        return ResponseEntity.ok(result);
    }
}
