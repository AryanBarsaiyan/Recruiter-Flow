package com.futurescope.platform.auth.web;

import com.futurescope.platform.auth.service.AuthService;
import com.futurescope.platform.auth.service.AuthService.AuthTokens;
import com.futurescope.platform.auth.web.dto.AcceptInviteRequest;
import com.futurescope.platform.auth.web.dto.AuthResponse;
import com.futurescope.platform.auth.web.dto.InviteRequest;
import com.futurescope.platform.auth.web.dto.InviteResponse;
import com.futurescope.platform.auth.web.dto.LoginRequest;
import com.futurescope.platform.auth.web.dto.RefreshRequest;
import com.futurescope.platform.auth.web.dto.RequestPasswordResetRequest;
import com.futurescope.platform.auth.web.dto.ResetPasswordRequest;
import com.futurescope.platform.auth.web.dto.SignupSuperAdminRequest;
import com.futurescope.platform.auth.web.dto.TokenRequest;
import com.futurescope.platform.auth.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup-super-admin")
    public ResponseEntity<AuthResponse> signupSuperAdmin(
            @Valid @RequestBody SignupSuperAdminRequest request
    ) {
        AuthTokens tokens = authService.signupSuperAdmin(request);
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthTokens tokens = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        AuthTokens tokens = authService.refresh(request);
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User currentUser) {
        authService.logout(currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invite")
    public ResponseEntity<InviteResponse> invite(
            @Valid @RequestBody InviteRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(authService.invite(request, currentUser));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<AuthResponse> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        AuthTokens tokens = authService.acceptInvite(request.getToken(), request.getPassword());
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken(), tokens.refreshToken()));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody TokenRequest request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody RequestPasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}

