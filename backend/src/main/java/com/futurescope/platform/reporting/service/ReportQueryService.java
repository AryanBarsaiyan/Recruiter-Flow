package com.futurescope.platform.reporting.service;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.reporting.domain.InterviewReport;
import com.futurescope.platform.reporting.repository.InterviewReportFactorRepository;
import com.futurescope.platform.reporting.repository.InterviewReportRepository;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import com.futurescope.platform.audit.service.AuditService;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportQueryService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final InterviewReportRepository reportRepository;
    private final InterviewReportFactorRepository factorRepository;
    private final RbacService rbacService;
    private final AuditService auditService;

    public ReportQueryService(
            JobApplicationRepository jobApplicationRepository,
            JobRepository jobRepository,
            InterviewReportRepository reportRepository,
            InterviewReportFactorRepository factorRepository,
            RbacService rbacService,
            AuditService auditService
    ) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.reportRepository = reportRepository;
        this.factorRepository = factorRepository;
        this.rbacService = rbacService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReportByApplicationId(UUID applicationId, User currentUser) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        rbacService.requireActiveCompanyMember(currentUser, application.getJob().getCompany().getId());
        InterviewReport report = reportRepository.findByApplication(application)
                .orElseThrow(() -> new IllegalArgumentException("Report not found for this application"));
        auditService.log(application.getJob().getCompany(), currentUser, null, "report_viewed", "application", applicationId, "{\"reportId\":\"" + report.getId() + "\"}");
        return toReportResponse(report);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummaryByJobId(UUID jobId, User currentUser) {
        var job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
        long total = jobApplicationRepository.countByJob(job);
        return Map.of(
                "jobId", jobId,
                "jobTitle", job.getTitle(),
                "totalApplications", total
        );
    }

    private Map<String, Object> toReportResponse(InterviewReport report) {
        List<Map<String, Object>> factors = factorRepository.findByReport(report).stream()
                .map(f -> Map.<String, Object>of(
                        "factorName", f.getFactorName(),
                        "weight", f.getWeight() != null ? f.getWeight() : 0,
                        "score", f.getScore() != null ? f.getScore() : 0,
                        "maxScore", f.getMaxScore() != null ? f.getMaxScore() : 0
                ))
                .collect(Collectors.toList());
        return Map.of(
                "id", report.getId(),
                "interviewId", report.getInterview().getId(),
                "applicationId", report.getApplication().getId(),
                "overallScore", report.getOverallScore() != null ? report.getOverallScore() : 0,
                "riskScore", report.getRiskScore() != null ? report.getRiskScore() : 0,
                "riskLevel", report.getRiskLevel() != null ? report.getRiskLevel() : "",
                "summary", report.getSummary() != null ? report.getSummary() : "",
                "generatedAt", report.getGeneratedAt(),
                "factors", factors
        );
    }

    @Transactional(readOnly = true)
    public String exportCsv(UUID jobId, UUID applicationId, User currentUser) {
        if (jobId != null) {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found"));
            rbacService.requireActiveCompanyMember(currentUser, job.getCompany().getId());
            List<JobApplication> applications = jobApplicationRepository.findByJobOrderByAppliedAtDesc(job);
            StringBuilder sb = new StringBuilder("applicationId,jobTitle,candidateId,status,appliedAt\n");
            for (JobApplication a : applications) {
                sb.append(a.getId()).append(",")
                        .append(escapeCsv(job.getTitle())).append(",")
                        .append(a.getCandidate().getId()).append(",")
                        .append(escapeCsv(a.getStatus())).append(",")
                        .append(a.getAppliedAt()).append("\n");
            }
            return sb.toString();
        }
        if (applicationId != null) {
            JobApplication application = jobApplicationRepository.findById(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException("Application not found"));
            rbacService.requireActiveCompanyMember(currentUser, application.getJob().getCompany().getId());
            String header = "applicationId,jobTitle,status\n";
            return header + application.getId() + "," + escapeCsv(application.getJob().getTitle()) + "," + escapeCsv(application.getStatus()) + "\n";
        }
        throw new IllegalArgumentException("Either jobId or applicationId must be provided");
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
