package com.futurescope.platform.schedule.web;

import com.futurescope.platform.schedule.service.SchedulingService;
import com.futurescope.platform.schedule.web.dto.BookSlotRequest;
import com.futurescope.platform.schedule.web.dto.InvitationInfoResponse;
import com.futurescope.platform.schedule.web.dto.SlotResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interview-invitations")
public class InterviewInvitationController {

    private final SchedulingService schedulingService;

    public InterviewInvitationController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<InvitationInfoResponse> getByToken(@PathVariable String token) {
        InvitationInfoResponse info = schedulingService.getInvitationByToken(token);
        return ResponseEntity.ok(info);
    }

    @PostMapping("/{token}/slots")
    public ResponseEntity<SlotResponse> bookSlot(
            @PathVariable String token,
            @Valid @RequestBody BookSlotRequest request
    ) {
        SlotResponse slot = schedulingService.bookSlot(token, request);
        return ResponseEntity.ok(slot);
    }
}
