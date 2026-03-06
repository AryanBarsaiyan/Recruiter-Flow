package com.futurescope.platform.proctoring.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.proctoring.domain.ProctoringEvent;
import com.futurescope.platform.proctoring.domain.ProctoringSession;
import com.futurescope.platform.proctoring.service.ProctoringService;
import com.futurescope.platform.proctoring.web.dto.ProctoringEventRequest;
import com.futurescope.platform.proctoring.web.dto.StartProctoringRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/proctoring/sessions")
public class ProctoringController {

    private final ProctoringService proctoringService;

    public ProctoringController(ProctoringService proctoringService) {
        this.proctoringService = proctoringService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> start(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody StartProctoringRequest request
    ) {
        ProctoringSession session = proctoringService.startSession(request.getInterviewId(), currentUser);
        return ResponseEntity.ok(Map.of(
                "id", session.getId(),
                "interviewId", session.getInterview().getId(),
                "startedAt", session.getStartedAt()
        ));
    }

    @PostMapping("/{sessionId}/events")
    public ResponseEntity<Map<String, Object>> postEvent(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID sessionId,
            @Valid @RequestBody ProctoringEventRequest request
    ) {
        ProctoringEvent event = proctoringService.postEvent(
                sessionId, request.getEventType(), request.getDetailsJson(), request.getWeight(), currentUser);
        return ResponseEntity.ok(Map.of(
                "id", event.getId(),
                "eventType", event.getEventType(),
                "occurredAt", event.getOccurredAt()
        ));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<Map<String, Object>> end(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID sessionId
    ) {
        ProctoringSession session = proctoringService.endSession(sessionId, currentUser);
        return ResponseEntity.ok(Map.of(
                "id", session.getId(),
                "endedAt", session.getEndedAt(),
                "overallRiskScore", session.getOverallRiskScore() != null ? session.getOverallRiskScore() : 0
        ));
    }
}
