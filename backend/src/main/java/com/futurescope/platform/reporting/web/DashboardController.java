package com.futurescope.platform.reporting.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dashboards")
public class DashboardController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final RbacService rbacService;

    public DashboardController(CompanyRepository companyRepository,
                               JobRepository jobRepository,
                               JobApplicationRepository jobApplicationRepository,
                               UserRepository userRepository,
                               RbacService rbacService) {
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.rbacService = rbacService;
    }

    @GetMapping("/recruiter")
    public ResponseEntity<Map<String, Object>> recruiterDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam UUID companyId
    ) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        long totalJobs = jobRepository.countByCompany(company);
        long totalApplications = jobApplicationRepository.countByJobCompanyId(companyId);
        return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "totalJobs", totalJobs,
                "totalApplications", totalApplications
        ));
    }

    @GetMapping("/platform-admin")
    public ResponseEntity<Map<String, Object>> platformAdminDashboard(
            @AuthenticationPrincipal User currentUser
    ) {
        if (!"platform_admin".equals(currentUser.getUserType())) {
            throw new IllegalArgumentException("Platform admin access required");
        }
        long totalCompanies = companyRepository.count();
        long totalUsers = userRepository.count();
        return ResponseEntity.ok(Map.of(
                "totalCompanies", totalCompanies,
                "totalUsers", totalUsers
        ));
    }
}
