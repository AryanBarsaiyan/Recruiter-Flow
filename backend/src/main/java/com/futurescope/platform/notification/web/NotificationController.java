package com.futurescope.platform.notification.web;

import com.futurescope.platform.auth.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> test(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Test notification sent"));
    }
}
