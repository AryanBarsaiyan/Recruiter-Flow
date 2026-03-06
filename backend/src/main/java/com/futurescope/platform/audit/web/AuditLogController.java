package com.futurescope.platform.audit.web;

import com.futurescope.platform.audit.domain.AuditLog;
import com.futurescope.platform.audit.repository.AuditLogRepository;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.service.RbacService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final RbacService rbacService;

    public AuditLogController(AuditLogRepository auditLogRepository, CompanyRepository companyRepository, RbacService rbacService) {
        this.auditLogRepository = auditLogRepository;
        this.companyRepository = companyRepository;
        this.rbacService = rbacService;
    }

    @GetMapping
    public ResponseEntity<Page<Map<String, Object>>> list(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId,
            Pageable pageable
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, companyId);
        Page<AuditLog> page = auditLogRepository.findByCompanyOrderByCreatedAtDesc(company, pageable);
        Page<Map<String, Object>> response = page.map(log -> Map.<String, Object>of(
                "id", log.getId(),
                "action", log.getAction(),
                "entityType", log.getEntityType(),
                "entityId", log.getEntityId() != null ? log.getEntityId() : "",
                "actorRole", log.getActorRole() != null ? log.getActorRole() : "",
                "createdAt", log.getCreatedAt(),
                "metadata", log.getMetadataJson() != null ? log.getMetadataJson() : "{}"
        ));
        return ResponseEntity.ok(response);
    }
}
