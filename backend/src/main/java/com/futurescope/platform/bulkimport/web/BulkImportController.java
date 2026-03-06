package com.futurescope.platform.bulkimport.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.bulkimport.domain.BulkImportBatch;
import com.futurescope.platform.bulkimport.repository.BulkImportBatchRepository;
import com.futurescope.platform.bulkimport.repository.BulkImportCandidateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bulk-import")
public class BulkImportController {

    private final CompanyRepository companyRepository;
    private final BulkImportBatchRepository batchRepository;
    private final BulkImportCandidateRepository candidateRepository;
    private final RbacService rbacService;

    public BulkImportController(CompanyRepository companyRepository,
                                BulkImportBatchRepository batchRepository,
                                BulkImportCandidateRepository candidateRepository,
                                RbacService rbacService) {
        this.companyRepository = companyRepository;
        this.batchRepository = batchRepository;
        this.candidateRepository = candidateRepository;
        this.rbacService = rbacService;
    }

    @GetMapping("/batches")
    public ResponseEntity<List<Map<String, Object>>> listBatches(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId
    ) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        List<BulkImportBatch> batches = batchRepository.findByCompanyOrderByCreatedAtDesc(company);
        List<Map<String, Object>> body = batches.stream()
                .map(b -> Map.<String, Object>of(
                        "id", b.getId(),
                        "companyId", b.getCompany().getId(),
                        "status", b.getStatus(),
                        "sourceFilePath", b.getSourceFilePath(),
                        "createdAt", b.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<Map<String, Object>> getBatch(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        BulkImportBatch batch = batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
        rbacService.requireActiveCompanyMember(currentUser, batch.getCompany().getId());
        long candidateCount = candidateRepository.countByBatch(batch);
        return ResponseEntity.ok(Map.of(
                "id", batch.getId(),
                "companyId", batch.getCompany().getId(),
                "status", batch.getStatus(),
                "sourceFilePath", batch.getSourceFilePath(),
                "createdAt", batch.getCreatedAt(),
                "candidateCount", candidateCount
        ));
    }
}
