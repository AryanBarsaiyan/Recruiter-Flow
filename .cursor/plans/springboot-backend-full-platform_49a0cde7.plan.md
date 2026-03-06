---
name: springboot-backend-full-platform
overview: Design and implement a full-featured Spring Boot 4.0.3 + PostgreSQL backend for the Recruitment & AI Interview Platform, covering all modules from BRD/HLD/LLD with an abstract AI integration layer and comprehensive automated tests.
todos:
  - id: setup-project
    content: Set up Spring Boot 4.0.3 project with Postgres, basic config, and Flyway migrations skeleton based on db-design.md.
    status: completed
  - id: implement-auth-rbac
    content: Implement auth, JWT security, user/company/role models, and RBAC enforcement with tests.
    status: completed
  - id: jobs-candidates-applications
    content: Implement jobs, candidates, applications, and pipelines modules with core APIs and tests.
    status: completed
  - id: ai-orchestration
    content: Implement abstract AI resume screening and interview scoring clients plus orchestration and tests.
    status: completed
  - id: interview-proctoring-reports
    content: Implement interview engine, proctoring, and reporting modules end-to-end with tests.
    status: completed
  - id: notifications-webhooks-audit
    content: Add notifications, webhooks, audit logging, observability, and performance hardening with automated test coverage.
    status: completed
isProject: false
---

## Spring Boot Backend – End-to-End Implementation Plan

### 1. Tech Stack & Project Setup

- **Backend framework**: Spring Boot 4.0.3 (Spring Framework 6, Java 21 recommended).
- **Build tool**: Maven or Gradle (choose one and standardize; assume Maven unless you prefer otherwise).
- **Persistence**: Spring Data JPA + Hibernate with PostgreSQL.
- **Security**: Spring Security 6 with JWT-based auth and method-level authorization.
- **Validation & Docs**: Bean Validation (Jakarta), Springdoc OpenAPI/Swagger.
- **Testing**: JUnit 5, Spring Boot Test, Testcontainers (for Postgres), Mockito, RestAssured/MockMvc.

Steps:

- Create a new Spring Boot 4.0.3 project (`future-scope-backend`) with base package `com.futurescope.platform`.
- Add dependencies: `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`, `postgresql`, `spring-boot-starter-validation`, Lombok (optional), `springdoc-openapi-starter-webmvc-ui`, testing libraries.
- Configure `application.yml` profiles: `dev`, `test`, `prod` (Postgres URL, user, password, Hibernate ddl-auto strategy; Flyway for migrations).
- Set up Flyway/Liquibase for schema migrations aligned with `db-design.md`.
- Configure basic CI pipeline steps (build, unit tests, integration tests).

### 2. Database Schema & Migrations (PostgreSQL)

- Translate `db-design.md` into normalized Flyway migration scripts:
  - **Auth & RBAC**: `users`, `companies`, `roles`, `company_members`, `user_sessions`, `email_verifications`, `password_resets`, `mfa_methods`.
  - **Candidates & Resumes**: `candidates`, `resumes`, `saved_jobs`, `bulk_import_batches`, `bulk_import_candidates`.
  - **Jobs & Applications**: `jobs`, `job_custom_fields`, `job_applications`, `application_answers`.
  - **Pipelines**: `pipelines`, `pipeline_stages`, `application_stage_progress`.
  - **AI Resume & Scheduling**: `resume_screenings`, `interview_invitations`, `interview_slots`.
  - **Interview Engine**: `question_bank_questions`, `question_test_cases`, `interviews`, `interview_questions`, `code_submissions`, `followup_questions`, `followup_answers`, `manual_interviews`.
  - **Proctoring & Behavior**: `proctoring_sessions`, `proctoring_events`, `behavioral_metrics`.
  - **Reporting & Integrations**: `interview_reports`, `interview_report_factors`, `notifications`, `webhook_events`, `audit_logs`.
- Add all necessary foreign keys, indexes, and JSONB columns as specified (including multi-tenant `company_id` on relevant tables).
- Write schema-level tests (via Testcontainers) to ensure migrations apply cleanly and constraints behave as intended.

### 3. Core Infrastructure & Cross-Cutting Concerns

- Implement global exception handling (`@ControllerAdvice`) with consistent error model (code, message, details).
- Configure logging (SLF4J + Logback) with correlation IDs and MDC to track requests, user IDs, and company IDs.
- Implement a standardized response envelope (where useful) and pagination model.
- Add request validation using DTOs and Bean Validation annotations.
- Set up mapping between entities and DTOs (MapStruct or manual mappers).

### 4. Security, Auth & RBAC

- Implement JWT-based auth with Spring Security:
  - `User` entity and `UserRepository` mapping to `users`.
  - Password hashing with BCrypt, login, logout, refresh token strategy, and enforcement of single active session via `user_sessions`.
  - Email verification and password reset flows using tokens in `email_verifications` and `password_resets`.
- Define roles and authorities based on `roles` and `company_members` (SuperAdmin, Admin, ReadOnly, PlatformAdmin).
- Add method/endpoint-level authorization annotations to enforce:
  - Company scoping and role checks for recruiter APIs.
  - Candidate-only access to their own resources.
- Implement optional MFA hooks (API endpoints and data model, with ability to plug in TOTP or email-OTP later).
- Write unit and integration tests for auth flows, including security rules and edge cases (invalid tokens, cross-company access attempts).

### 5. Company & RBAC Module

- Implement `Company`, `Role`, `CompanyMember` entities and repositories.
- Expose APIs:
  - Super Admin signup (`POST /auth/signup-super-admin`).
  - Invite members, accept invites, manage roles (`/companies/{id}/members` endpoints).
  - Company onboarding and basic branding config.
- Implement tenant resolution (from URL, request headers, or authenticated context) and ensure all company-scoped queries filter by `company_id`.
- Add tests for tenant isolation (no cross-company data leakage) and role behaviors.

### 6. Candidate & Profile Module

- Implement `Candidate`, `Resume`, `SavedJob`, `BulkImport` entities.
- Expose APIs for:
  - Candidate auto-creation on first application.
  - Candidate profile read/update (`/me`), application list, saved-jobs list and CRUD.
  - Bulk import listing and inspection (recruiter-side).
- Integrate with storage abstraction (e.g., AWS S3-compatible interface) for resume file handling; store only paths in DB.
- Write tests for candidate lifecycle and consistency between `users` and `candidates`.

### 7. Job & Application Module

- Implement `Job`, `JobCustomField`, `JobApplication`, `ApplicationAnswer` entities and services.
- APIs:
  - Recruiter job CRUD, job configuration (resume criteria, custom form schema, scoring weights, pipeline association).
  - Public job board listing (`/jobs/public`) with filters; application deadline and `is_published` logic.
  - Candidate job application submission (`POST /jobs/{jobId}/apply`) including resume upload and dynamic custom fields.
  - Candidate application status and history.
- Ensure data integrity (single `JobApplication` per candidate/job constraint if required, or track duplicates clearly).
- Add pagination, sorting, and filtering for recruiter views.
- Tests covering job lifecycle, application submission, validation, and basic funnel metrics.

### 8. Pipeline & Stage Orchestration

- Implement `Pipeline`, `PipelineStage`, `ApplicationStageProgress` entities and a pipeline orchestrator service.
- Provide APIs for recruiters to:
  - Configure pipelines per company and assign them to jobs.
  - View and, if needed, manually override candidate stage/status.
- Implement stage transition logic based on events (resume screening result, interview completion, manual decisions).
- Write unit tests for pipeline state machine transitions and edge cases (skipped, failed, manual_review).

### 9. AI Resume Screening (Abstract Integration)

- Define an `AiResumeScreeningClient` interface and DTOs (input: parsed resume + criteria; output: score/result/explanation).
- Implement an in-memory/mock adapter for dev/test, with the ability to plug in any real provider later.
- Implement `ResumeScreening` entities and worker-style services that:
  - Consume new `JobApplication` events and perform asynchronous screening.
  - Persist results to `resume_screenings` and update `job_applications` and `application_stage_progress`.
  - Create `interview_invitations` and trigger notifications for shortlisted applications.
- Write tests using the mock AI client to verify orchestration logic and ensure deterministic behavior.

### 10. Scheduling & Invitations

- Implement `InterviewInvitation` and `InterviewSlot` entities and services.
- APIs:
  - Invitation retrieval and validation using tokens.
  - Time slot selection, rescheduling, cancellation, no-show tracking, and rules enforcement.
- Integrate with notifications to send email/SMS reminders (through an abstract `NotificationClient`).
- Tests for various scheduling scenarios, including token expiry, multiple reschedules, and no-show handling.

### 11. Interview Engine (AI DSA)

- Implement question bank and interview entities per LLD:
  - CRUD APIs for `question_bank_questions` and `question_test_cases` (recruiter-side, company-scoped).
  - Interview session lifecycle: start, question assignment, code submission, follow-up questions, completion.
- Define an `AiInterviewScoringClient` interface for:
  - Generating follow-up questions (or parameterizing static templates).
  - Scoring code submissions and follow-up answers.
  - Computing overall scores based on configurable weights.
- Implement a code execution abstraction to run candidate code against test cases (can start with a mocked executor or a simple in-process sandbox for one language).
- Align scoring and factor breakdown with `BRD` weights and store in `interviews`, `code_submissions`, `followup` tables.
- Tests:
  - Unit tests of scoring logic, factor combination, and sequence (Q1 → follow-ups → Q2).
  - Integration tests for full interview flow using mock AI and code executor.

### 12. Proctoring & Anti-Cheating

- Implement `ProctoringSession`, `ProctoringEvent`, `BehavioralMetric` entities and services.
- APIs for the front-end to:
  - Start/end proctoring sessions tied to an `interview`.
  - Post events for tab/screen switches, devtools open, copy/paste, inactivity.
- Implement a risk scoring service that aggregates events and behavioral metrics into a risk score and summary.
- Feed risk score into `interview_reports` and dashboards.
- Tests for risk calculation, event ingestion, and security/authorization of proctoring endpoints.

### 13. Reporting & Dashboards

- Implement `InterviewReport` and `InterviewReportFactor` entities and services that:
  - Aggregate scores from interview engine and risk from proctoring.
  - Persist a stable, versioned snapshot of the report per `interview`.
- APIs:
  - Recruiter-facing report retrieval per application/interview.
  - Candidate-facing report retrieval with an appropriate subset of fields.
  - Job-level and company-level summaries for recruiter dashboards (funnel stats, average scores, risk distribution, time-to-hire).
  - Platform admin dashboard APIs.
- Implement optional export endpoints (PDF/CSV) with appropriate access control.
- Tests for aggregation correctness and performance-sensitive queries (using Testcontainers where needed).

### 14. Notifications & Webhooks

- Implement `Notification` and `WebhookEvent` models and services.
- Abstract email/SMS/webhook sending behind interfaces (`EmailSender`, `SmsSender`, `WebhookDispatcher`) with mock and real adapters.
- Trigger notifications on key events: candidate applied, shortlisted, invited, scheduled, interview completed, passed/failed, password reset, etc.
- Provide configuration APIs for company-level webhook endpoints and notification templates.
- Tests:
  - Unit tests for notification orchestration and retry logic.
  - Integration tests verifying webhook event lifecycles and idempotency.

### 15. Audit Logging & Compliance

- Implement `AuditLog` model and a cross-cutting audit service to record sensitive actions (invites, role changes, job publish, report view/export).
- Integrate audit logging in controllers/services using AOP or explicit calls.
- Implement data retention mechanisms (e.g., scheduled jobs to prune/archive logs and proctoring data per policy).
- Tests to ensure audit entries are created for critical paths and that retention jobs behave correctly.

### 16. Observability, Performance & Hardening

- Integrate metrics (Micrometer) and health checks (Spring Boot Actuator) for key components.
- Add structured logging and error alerts for AI integration failures, queue backlogs, and slow queries.
- Introduce rate limiting on auth, interview, and proctoring endpoints (via filters or external gateway).
- Perform performance profiling on critical flows (resume screening throughput, interview start latency) and optimize indexes/queries.

### 17. Testing Strategy & Coverage

- **Unit tests**: Services, domain logic, utility classes (e.g., scoring, risk aggregation, pipeline transitions).
- **Web layer tests**: Controllers via MockMvc/RestAssured with security filters active.
- **Integration tests**: Repository tests and end-to-end flows using Testcontainers Postgres and mock AI/notification clients.
- **Contract/API tests**: Validate OpenAPI spec and sample client calls.
- **Test data builders**: Reusable fixtures for creating users, companies, jobs, candidates, and interviews.
- Aim for high coverage on critical modules (auth, RBAC, jobs/applications, interview, proctoring, reporting) and smoke tests across all others.

### 18. Phased Implementation & Milestones

- **Milestone 1**: Project setup, DB migrations, auth & RBAC core, basic company onboarding.
- **Milestone 2**: Jobs, candidates, applications, pipelines (resume screening stage wired with mock AI).
- **Milestone 3**: Interview invitations, scheduling, AI DSA interview engine (mock AI + basic code execution), proctoring events ingestion.
- **Milestone 4**: Reporting, dashboards, notifications, and webhooks.
- **Milestone 5**: Audit logging, observability, performance tuning, and hardening for production readiness.
- For each milestone, enforce completion criteria: all relevant APIs implemented, OpenAPI docs updated, and tests (unit + integration) passing.
