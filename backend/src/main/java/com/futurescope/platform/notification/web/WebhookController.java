package com.futurescope.platform.notification.web;

import com.futurescope.platform.auth.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId
    ) {
        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "endpoints", java.util.List.of()
        ));
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, String>> saveConfig(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Object> body
    ) {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Webhook config updated"));
    }
}
