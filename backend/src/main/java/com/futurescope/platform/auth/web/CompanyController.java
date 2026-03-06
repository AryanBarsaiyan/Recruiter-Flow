package com.futurescope.platform.auth.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.CompanyService;
import com.futurescope.platform.auth.web.dto.CompanyMemberResponse;
import com.futurescope.platform.auth.web.dto.CompanyResponse;
import com.futurescope.platform.auth.web.dto.UpdateBrandingRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        CompanyResponse response = companyService.getById(id, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<CompanyMemberResponse>> listMembers(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        List<CompanyMemberResponse> list = companyService.listMembers(id, currentUser);
        return ResponseEntity.ok(list);
    }

    @PatchMapping("/{id}/branding")
    public ResponseEntity<CompanyResponse> updateBranding(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBrandingRequest request
    ) {
        CompanyResponse response = companyService.updateBranding(id, currentUser, request);
        return ResponseEntity.ok(response);
    }
}
