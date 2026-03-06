package com.futurescope.platform;

import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.Role;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyMemberRepository;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.repository.RoleRepository;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.domain.QuestionBankQuestion;
import com.futurescope.platform.interview.repository.InterviewRepository;
import com.futurescope.platform.interview.repository.QuestionBankQuestionRepository;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.repository.JobRepository;
import com.futurescope.platform.reporting.domain.InterviewReport;
import com.futurescope.platform.reporting.repository.InterviewReportRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Builds test data for E2E tests. Use in tests that need companies, jobs, questions, candidates, reports.
 */
@Component
public class TestDataHelper {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final JobRepository jobRepository;
    private final QuestionBankQuestionRepository questionBankQuestionRepository;
    private final CandidateRepository candidateRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewReportRepository reportRepository;

    public TestDataHelper(
            CompanyRepository companyRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            CompanyMemberRepository companyMemberRepository,
            JobRepository jobRepository,
            QuestionBankQuestionRepository questionBankQuestionRepository,
            CandidateRepository candidateRepository,
            InterviewRepository interviewRepository,
            InterviewReportRepository reportRepository
    ) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.jobRepository = jobRepository;
        this.questionBankQuestionRepository = questionBankQuestionRepository;
        this.candidateRepository = candidateRepository;
        this.interviewRepository = interviewRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public Company createCompany(String name, String slug) {
        Company c = new Company();
        c.setId(UUID.randomUUID());
        c.setName(name);
        c.setSlug(slug);
        c.setActive(true);
        c.setCreatedAt(OffsetDateTime.now());
        return companyRepository.save(c);
    }

    @Transactional
    public User createRecruiter(String email, String passwordHash) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setUserType("recruiter");
        u.setActive(true);
        u.setCreatedAt(OffsetDateTime.now());
        return userRepository.save(u);
    }

    @Transactional
    public Role ensureSuperAdminRole() {
        return roleRepository.findByNameAndScope("SuperAdmin", "company")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setId(UUID.randomUUID());
                    r.setName("SuperAdmin");
                    r.setScope("company");
                    return roleRepository.save(r);
                });
    }

    @Transactional
    public CompanyMember addMember(Company company, User user, Role role) {
        CompanyMember m = new CompanyMember();
        m.setId(UUID.randomUUID());
        m.setCompany(company);
        m.setUser(user);
        m.setRole(role);
        m.setStatus("active");
        m.setCreatedAt(OffsetDateTime.now());
        return companyMemberRepository.save(m);
    }

    @Transactional
    public Job createJob(Company company, User createdBy, String title) {
        Job j = new Job();
        j.setId(UUID.randomUUID());
        j.setCompany(company);
        j.setTitle(title);
        j.setCreatedBy(createdBy);
        j.setPublished(true);
        OffsetDateTime now = OffsetDateTime.now();
        j.setCreatedAt(now);
        j.setUpdatedAt(now);
        return jobRepository.save(j);
    }

    @Transactional
    public void createTwoQuestionsForCompany(Company company, Job job) {
        for (int i = 1; i <= 2; i++) {
            QuestionBankQuestion q = new QuestionBankQuestion();
            q.setId(UUID.randomUUID());
            q.setCompany(company);
            q.setJob(job);
            q.setTitle("Question " + i);
            q.setDescription("Describe problem " + i);
            q.setStarterCode("// starter");
            q.setDifficulty("medium");
            q.setMaxScore(BigDecimal.valueOf(50));
            q.setActive(true);
            q.setCreatedAt(OffsetDateTime.now());
            questionBankQuestionRepository.save(q);
        }
    }

    @Transactional
    public User createCandidateUser(String email, String passwordHash) {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setUserType("candidate");
        u.setActive(true);
        u.setCreatedAt(OffsetDateTime.now());
        return userRepository.save(u);
    }

    @Transactional
    public Candidate createCandidateProfile(User user, String fullName) {
        Candidate c = new Candidate();
        c.setId(UUID.randomUUID());
        c.setUser(user);
        c.setFullName(fullName);
        c.setCreatedAt(OffsetDateTime.now());
        return candidateRepository.save(c);
    }

    @Transactional
    public InterviewReport createReportForInterview(Interview interview) {
        InterviewReport r = new InterviewReport();
        r.setId(UUID.randomUUID());
        r.setInterview(interview);
        r.setApplication(interview.getApplication());
        r.setOverallScore(BigDecimal.valueOf(75));
        r.setRiskScore(BigDecimal.valueOf(10));
        r.setRiskLevel("low");
        r.setSummary("Test report");
        r.setGeneratedAt(OffsetDateTime.now());
        return reportRepository.save(r);
    }
}
