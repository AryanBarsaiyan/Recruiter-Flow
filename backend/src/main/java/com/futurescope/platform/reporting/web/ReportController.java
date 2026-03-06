package com.futurescope.platform.reporting.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.reporting.service.ReportQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportQueryService reportQueryService;

    public ReportController(ReportQueryService reportQueryService) {
        this.reportQueryService = reportQueryService;
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<Map<String, Object>> getByApplication(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID applicationId
    ) {
        Map<String, Object> report = reportQueryService.getReportByApplicationId(applicationId, currentUser);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getSummaryByJob(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID jobId
    ) {
        Map<String, Object> summary = reportQueryService.getSummaryByJobId(jobId, currentUser);
        return ResponseEntity.ok(summary);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> export(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) UUID applicationId
    ) {
        String csv = reportQueryService.exportCsv(jobId, applicationId, currentUser);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "report-export.csv");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
