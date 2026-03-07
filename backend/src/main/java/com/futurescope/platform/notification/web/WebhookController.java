package com.futurescope.platform.notification.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.RbacService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final RbacService rbacService;

    public WebhookController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId
    ) {
        rbacService.requireActiveCompanyMember(currentUser, companyId);
        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "endpoints", List.of()
        ));
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, String>> saveConfig(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Object> body
    ) {
        Object companyIdObj = body.get("companyId");
        UUID companyId;
        if (companyIdObj instanceof UUID u) {
            companyId = u;
        } else if (companyIdObj instanceof String s) {
            companyId = UUID.fromString(s);
        } else {
            throw new IllegalArgumentException("companyId is required");
        }
        var member = rbacService.requireActiveCompanyMember(currentUser, companyId);
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Webhook config updated"));
    }
}
