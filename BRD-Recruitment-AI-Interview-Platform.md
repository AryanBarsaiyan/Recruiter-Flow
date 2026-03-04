# Business Requirements Document (BRD)
# Recruitment & AI Interview Platform

**Document Version:** 2.0  
**Last Updated:** March 1, 2026  
**Status:** Final (Requirements Locked; pricing/targets can be parameterized)  
**Stakeholders:** Product, Engineering, Recruiters, HR

---

## Table of Contents
1. [Executive Summary](#1-executive-summary)
2. [Project Overview](#2-project-overview) — *Scope & [USPs](#23-unique-selling-propositions-usps)*
3. [Goals & Objectives](#3-goals--objectives)
4. [User Personas](#4-user-personas)
5. [Functional Requirements](#5-functional-requirements)
6. [Non-Functional Requirements](#6-non-functional-requirements)
7. [User Flows](#7-user-flows)
8. [System Components](#8-system-components)
9. [Backlog (Nice-to-have)](#9-backlog-nice-to-have)
10. [Future Scope: Hiring Pipeline Extension](#10-future-scope-hiring-pipeline-extension)
11. [Assumptions & Constraints](#11-assumptions--constraints)
12. [Competitor Analysis](#12-competitor-analysis)
13. [Business Model & Pricing Strategy](#13-business-model--pricing-strategy)
14. [Market Analysis](#14-market-analysis)
15. [Success Metrics & KPIs](#15-success-metrics--kpis)
16. [Risk & Mitigation](#16-risk--mitigation)
17. [API & Integration Architecture](#17-api--integration-architecture)
18. [Enterprise Security & Compliance](#18-enterprise-security--compliance)
19. [AI Governance & Transparency](#19-ai-governance--transparency)
20. [Scalability & Performance](#20-scalability--performance)
21. [Analytics & Dashboard System](#21-analytics--dashboard-system)
22. [Advanced Anti-Cheating Layer](#22-advanced-anti-cheating-layer)
23. [Strategic Positioning](#23-strategic-positioning)
24. [Glossary](#24-glossary)

---

## 1. Executive Summary

This BRD defines the business and functional requirements for a **Recruitment & AI Interview Platform** that enables recruiters and companies to post jobs, onboard candidates (students), screen resumes with AI, and conduct proctored AI-driven technical (DSA) interviews with anti-cheating measures and automated scoring.

The platform supports two primary user types: **Recruiters** (companies, admins, read-only users) and **Students** (candidates who apply to jobs and take AI interviews). Key differentiators include AI resume matching, structured DSA interviews with authenticity checks, and comprehensive proctoring (full-screen, tab/screen switch alerts, copy logging, and risk scoring).

**Enterprise-ready scope (v2.0):** This BRD now includes competitor analysis, business model & pricing, market analysis, success metrics, risk mitigation, API & integration architecture, security & compliance framework, AI governance, scalability targets, analytics dashboards, advanced anti-cheating, and strategic positioning—making it suitable for investor and enterprise review.

---

## 2. Project Overview

### 2.1 Purpose
- Centralize recruitment for companies with role-based access (super admin, admin, read-only).
- Provide a job board where students can discover and apply to jobs via forms.
- Automate resume screening using AI and trigger AI interview invitations when there is a fit.
- Conduct fair, proctored technical (DSA) interviews with anti-cheating controls and detailed, factor-based scoring (out of 100).

### 2.2 Scope
| In Scope | Out of Scope |
|----------|--------------|
| Recruiter signup (super admin), invites, permissions | Native mobile apps (can be later) |
| Company onboarding (unique companies), branding, audit logs | Payroll / HRIS integration |
| Job posting and job board (saved jobs, application status) | Legal/compliance deep-dives (refer to legal) |
| Custom application forms per job | Offline interview mode |
| Student application forms and account creation on first apply | — |
| Bulk student onboarding via sheet upload | |
| AI resume screening with recruiter-defined criteria + explainable match | |
| Custom question bank per company/job | |
| Email/SMS reminders for interviews | |
| Recruiter dashboard + candidate comparison | |
| Platform super-admin dashboard | |

### 2.3 Unique Selling Propositions (USPs)
| USP | Description |
|-----|-------------|
| **AI-first screening** | Resume is scanned by AI against job criteria; only shortlisted candidates get AI interview slots—reduces recruiter load and speeds up funnel. |
| **Authenticity verification** | After first code submission, AI asks follow-up questions on the solution to verify the candidate wrote it and understands it—reduces copy-paste and proxy cheating. |
| **End-to-end proctoring** | Full-screen enforcement, screen/tab switch alerts, inspect disabled, and copy-event logging; violations feed into a risk factor for fair, transparent assessment. |
| **Structured DSA + report** | Two DSA questions with test-case evaluation and factor-wise score (out of 100), giving recruiters a comparable, actionable report per candidate. |
| **Zero-friction for students** | No signup to apply; student account is created on first application; one account for multiple companies and jobs. |
| **Flexible onboarding** | Recruiters can either use the job board (students apply via form) or bulk-import candidates via sheet—same pipeline to AI interview and report. |

---

## 3. Goals & Objectives

| Goal | Objective |
|------|------------|
| **Recruiter efficiency** | Reduce time-to-hire via AI screening and structured interviews. |
| **Quality of hire** | Ensure candidate authenticity via code-questioning and proctoring. |
| **Scalability** | Support multiple companies, roles, and candidates on one platform. |
| **Fairness & transparency** | Clear rules (full-screen, no inspect, copy logging) and factor-based scores. |
| **Candidate experience** | Simple apply flow and clear interview expectations. |

---

## 4. User Personas

### 4.1 Recruiter Users
- **Super Admin:** First signup; can invite members, assign admin/read-only, manage company.
- **Admin:** Can post jobs, manage candidates, view reports, invite users (if permitted).
- **Read-only User:** Can view jobs, candidates, and reports; no edit/post permissions.

### 4.2 Student Users
- **Student/Candidate:** Discovers jobs, fills application forms, gets account on first apply, takes AI interview when invited, receives feedback/report.

### 4.3 System
- **AI:** Resume scanning, interview conduction, code questioning, scoring, report generation.

---

## 5. Functional Requirements

### 5.1 Authentication & User Management

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-AUTH-01 | Expose **signup** for users in **Super Admin** mode (first user of a company). | Must |
| FR-AUTH-02 | Super Admin can **invite** other members via email/link. | Must |
| FR-AUTH-03 | Support **roles**: Super Admin, Admin, Read-only User; assign per invite. | Must |
| FR-AUTH-04 | Invited users get signup/login flow and inherit assigned role. | Must |
| FR-AUTH-05 | **Company onboarding:** Support unique companies; each company has its own workspace/members. | Must |
| FR-AUTH-06 | Student account is **created on first job application** (no pre-signup required to browse/apply). | Must |
| FR-AUTH-07 | Students can have **one account** and apply to **multiple jobs** across different companies. | Must |
| FR-AUTH-08 | Enforce **single active login session** per user; when the same user logs in on a new device/browser, any previous active session token is automatically invalidated. | Must |

---

### 5.2 Company & Recruiter Features

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-COMP-01 | Recruiter (admin/super admin) can **onboard their company** (name, details, branding if needed). | Must |
| FR-COMP-02 | Recruiter can **post jobs** on the platform job board (title, description, requirements, resume criteria). | Must |
| FR-COMP-03 | Recruiter can **bulk onboard students** by uploading a **sheet** (CSV/Excel); system creates/links student accounts. | Must |
| FR-COMP-04 | Recruiter can view list of applicants per job and their status (applied, screened, invited, interviewed, reported). | Must |
| FR-COMP-05 | Recruiter can view **AI-generated student reports** and scores (out of 100, factor breakdown). | Must |
| FR-COMP-06 | Support **company branding**: custom logo, colors/theme, and recruiter email templates (per company). | Must |
| FR-COMP-07 | Support **custom application form fields per job** (text, dropdowns, checkboxes, file uploads; required/optional). | Must |
| FR-COMP-08 | Maintain an **audit log** for sensitive actions (invite sent/accepted, role changes, job create/update/publish, report view/export). | Must |
| FR-COMP-09 | Provide **Recruiter Dashboard**: total applicants, stage-wise pass rate, average score, and risk distribution (filter by job/date). | Must |
| FR-COMP-10 | Provide **Candidate Compare** view (side-by-side scores, factor breakdown, risk, and key notes). | Must |
| FR-COMP-11 | Provide a **Platform Super-admin Dashboard** (platform-level): companies, jobs, users, usage/volume, system health. | Must |

---

### 5.3 Job Board & Student Application

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-JOB-01 | **Job board** is visible to students (public or after minimal gate; no account required to view jobs). | Must |
| FR-JOB-02 | Student **fills application form** per job (e.g. name, email, resume upload, optional fields). | Must |
| FR-JOB-03 | On first form submit, system **creates a unique student account** (email as identifier; password/setup flow as needed). | Must |
| FR-JOB-04 | Same student can **apply to multiple jobs** (different companies) using the same account. | Must |
| FR-JOB-05 | Application form may include resume upload and basic eligibility fields as defined by job. | Should |
| FR-JOB-06 | Student can **save jobs** for later and manage a **Saved Jobs** list. | Must |
| FR-JOB-07 | Student has an **Application Status** page showing each application’s stage (applied, screened, invited, scheduled, completed, passed/failed). | Must |

---

### 5.4 AI Resume Screening & Interview Invitation

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-AI-01 | On application submit, system **scans the uploaded resume with AI** against job criteria set by recruiter. | Must |
| FR-AI-02 | If resume **meets** the job criteria, system **sends an AI interview invitation** with **time slot selection**. | Must |
| FR-AI-03 | Student can **choose a time slot** from available options; confirmation stored. | Must |
| FR-AI-04 | If resume does not meet criteria, store result and optionally notify student (e.g. “under review” or “not shortlisted”). | Should |
| FR-AI-05 | Recruiter can configure **resume matching criteria** per job (keywords/skills, experience range, must-have vs nice-to-have, weights). | Must |
| FR-AI-06 | Provide **explainable match** output: short reasons for match/no-match for recruiter and optionally for student (configurable). | Must |

---

### 5.5 AI Interview – Structure & Content

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-INT-01 | Interview includes **two DSA (Data Structures & Algorithms) questions**. | Must |
| FR-INT-02 | **Second question** is presented **after** the user submits the first question’s solution. | Must |
| FR-INT-03 | After user submits the **first code**, system **asks follow-up questions** about that solution (e.g. complexity, approach, edge cases) to verify **authenticity** and reduce cheating. | Must |
| FR-INT-04 | AI evaluates: (1) code submission, (2) test case results, (3) discussion answers. | Must |
| FR-INT-05 | System generates a **detailed report** for the student including factor-wise breakdown. | Must |
| FR-INT-06 | System **rates the student out of 100** on **several factors** (e.g. code correctness, complexity, explanation quality, test cases passed). | Must |
| FR-INT-07 | Support a **custom question bank** per company and/or per job (question CRUD, tagging by topic/difficulty, and assignment rules). | Must |

---

### 5.6 AI Interview – Proctoring & Anti-Cheating

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-PROCT-01 | Interview runs in **full-screen mode** (enforced where technically possible). | Must |
| FR-PROCT-02 | **Screen switch** (e.g. Alt+Tab, other monitor) triggers an **alert** and is **recorded**. | Must |
| FR-PROCT-03 | **Tab switch** (browser tab change) triggers an **alert** and is **recorded**. | Must |
| FR-PROCT-04 | **Disable or detect use of browser Inspect/DevTools** during the interview. | Must |
| FR-PROCT-05 | **Log copy events**: record **what the user copies** (e.g. snippet of code/text) and timestamp. | Must |
| FR-PROCT-06 | All violations (screen switch, tab switch, inspect, suspicious copy) **contribute to a risk/cheating factor** in the final report. | Must |
| FR-PROCT-07 | Final report includes **proctoring summary** and **risk score** (or risk level). | Must |

---

### 5.7 Reports & Scoring

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-REP-01 | Report includes: overall score (0–100), factor-wise scores, code evaluation, discussion summary, test cases passed/failed. | Must |
| FR-REP-02 | Report includes proctoring events and risk factor. | Must |
| FR-REP-03 | Recruiter (admin/read-only) can view and export (e.g. PDF) student reports per job. | Should |
| FR-REP-04 | System sends **Email/SMS reminders** for interview slot and pre-interview instructions (configurable templates, per company). | Must |

---

## 6. Non-Functional Requirements

| ID | Requirement | Category |
|----|-------------|----------|
| NFR-01 | Resume scan and interview start should complete within defined SLA (e.g. &lt; 2 min for scan). | Performance |
| NFR-02 | Interview session must be recoverable (e.g. resume from disconnect) within a short window. | Reliability |
| NFR-03 | All invite links, tokens, and session data must be handled securely (HTTPS, secure storage). | Security |
| NFR-04 | PII (resumes, names, emails) must be stored and processed per data retention and privacy policy. | Compliance |
| NFR-05 | UI must be usable on common desktop browsers (Chrome, Edge, Firefox) for interview. | Usability |
| NFR-06 | Proctoring and copy-logging must be disclosed to the candidate (consent/terms). | Legal/UX |

---

## 7. User Flows

### 7.1 Recruiter: Signup → Company → Job → Candidates
1. User signs up as **Super Admin**.
2. Onboards **company** (name, details).
3. **Invites** members with role (Admin / Read-only).
4. **Posts job(s)** on the job board.
5. (Optional) **Bulk uploads** student sheet to onboard candidates.
6. Views **applicants** and **AI reports** for each student.

### 7.2 Student: Apply → Account → Interview → Report
1. Student sees **job board**, selects a job.
2. Fills **application form** (resume + fields).
3. On first submit → **student account created**.
4. **AI scans resume**; if match → **interview invitation** with **time slot**.
5. Student **selects slot** and receives confirmation.
6. At slot time, student joins **AI interview**: full-screen, proctoring on.
7. **Question 1** → submit code → **follow-up questions** on that code.
8. **Question 2** (DSA) → submit code → (optional) follow-up.
9. System generates **report** (score out of 100, factors, risk); student and recruiter can view.

### 7.3 Bulk Onboarding (Recruiter)
1. Recruiter uploads **sheet** (CSV/Excel) with student details (e.g. name, email, college).
2. System **creates/links student accounts** and optionally associates with a job or “pool”.
3. Recruiter can then trigger **AI interview invitations** or next steps for these students.

---

## 8. System Components

| Component | Responsibility |
|-----------|----------------|
| **Auth & RBAC** | Signup, login, invites, roles (Super Admin, Admin, Read-only), company association. |
| **Company & Job Management** | Company onboarding, job CRUD, job board listing, branding (logo/colors/templates). |
| **Application & Student** | Application form, resume upload, student account creation, multi-apply. |
| **Bulk Import** | Parse sheet, validate, create/update student records. |
| **AI Resume Service** | Scan resume vs job criteria; return match/no-match with explainability; trigger invite flow. |
| **Scheduling** | Time slots, booking, Email/SMS reminders for AI interview. |
| **Interview Engine** | DSA questions, code run/test, follow-up Q&A, full-screen & proctoring hooks. |
| **Proctoring Service** | Full-screen, visibility/tab/screen switch, inspect detection, copy logging, violation storage. |
| **Scoring & Report** | Factor-wise score (0–100), risk factor, report generation, recruiter view/export. |
| **Analytics & Dashboards** | Recruiter dashboards, candidate compare, platform super-admin dashboard. |
| **Audit Logging** | Immutable audit trail for security/compliance and enterprise review. |

---

## 9. Backlog (Nice-to-have)

### 9.1 Recruiter & Company
- **Multi-company per group:** One group/enterprise with multiple companies (e.g. group admin).
- **Job templates** and duplicate job for similar roles.
- **Approval workflow** for job post (e.g. read-only approver).
- **SSO / LDAP** for enterprise recruiters.

### 9.2 Job Board & Applications
- **Calendar sync** (Google/Outlook) for interview slot.
- **Withdraw application** and **reapply after cooldown** (if allowed).
- **Application deadline** and **max applications per job** (caps).

### 9.3 AI & Screening
- **Re-screening** when job criteria are updated.
- **Multiple AI interview types:** DSA only, DSA + system design, DSA + behavioral.
- **Practice mode** for students (non-evaluated DSA run).

### 9.4 Interview & Proctoring
- **Live proctoring** (human watcher) in addition to automated.
- **Video/audio recording** (with consent) for review.
- **Plagiarism check** on submitted code (e.g. against known solutions).
- **Randomized question order** and **question pools** to reduce leakage.
- **Timer per question** and **total interview timer** with warnings.
- **Mobile-friendly interview** (e.g. responsive; proctoring limitations documented).
- **Retake policy:** One free retake per job with different questions.

### 9.5 Reports & Analytics
- **Export to ATS** (CSV, or ATS API integration).
- **Student-facing report** with tips to improve (e.g. “practice complexity”).
- **Factor customization:** Recruiter can weight factors (e.g. code 50%, communication 30%, proctoring 20%).

### 9.6 Platform & Ops
- **Email/SMS notifications** (configurable templates).
- **Multi-language** for UI and emails (e.g. EN, Hindi).
- **Accessibility** (WCAG) for application and interview UI.
- **API for ATS/HRIS** (post result, sync jobs).

---

## 10. Future Scope: Hiring Pipeline Extension

The hiring pipeline can be **extended and configured per recruiter** (or per job). Recruiters define the sequence of stages; candidates progress through each stage based on pass/fail outcomes.

### 10.1 Example Pipeline Stages

| Stage | Description |
|-------|--------------|
| **1. Resume Screening** | AI scans resume against job criteria; shortlisted candidates move to next stage. |
| **2. MCQ Round** | Multiple-choice questions (aptitude, domain, or technical); auto-graded; pass threshold set by recruiter. |
| **3. AI Interview** | Proctored DSA interview (as defined in current scope); report and score out of 100. |
| **4. Manual Interview(s)** | Human interviewer conducts live interview; recruiter sends invite, records session, submits review, and marks pass/fail. |

Recruiters can add, remove, or reorder stages (e.g. add another MCQ round, skip AI interview for certain roles).

### 10.2 Manual Interview Features

| Feature | Description |
|---------|-------------|
| **Google Meet invite** | Recruiter sends interview invite with Google Meet link; candidate receives link (email/in-app) with date/time. |
| **Recording** | Manual interview is recorded (video/audio); recording is initiated from within the platform or via Meet recording. |
| **Save recording** | Recording is **saved and stored** in the platform; linked to the candidate and job for later review. |
| **Review submission** | Manual interviewer can **submit a written review** (feedback, notes, strengths, concerns) after the interview. |
| **Pass/Fail decision** | Interviewer marks the candidate as **Pass** or **Fail** for that round; result updates the pipeline and candidate status. |

### 10.3 Pipeline Flow (Future)

```
Application → Resume Screening → MCQ Round → AI Interview → Manual Interview(s) → Offer/Reject
                    ↓                    ↓              ↓                    ↓
               (Pass/Fail)           (Pass/Fail)   (Score/Report)      (Pass/Fail + Review)
```

---

## 11. Assumptions & Constraints

### Assumptions
- Recruiters and students have stable internet and a desktop browser for the interview.
- Resume format (PDF/DOC) is acceptable for AI parsing.
- One “company” maps to one tenant/organization for the scope.
- Time slots are in a single timezone or timezone-aware (to be defined).
- Legal consent for proctoring and copy logging is obtained (terms/checkbox) before interview.

### Constraints
- Proctoring accuracy is limited by browser APIs (e.g. full-screen, visibility, copy).
- AI scoring may need calibration over time; human override or review may be needed.
- Bulk sheet format (columns) will be defined and validated (e.g. required: email, name).

---

## 12. Competitor Analysis

### 12.1 Primary Competitors (Technical Interview Platforms)

| Competitor | Strengths | Weaknesses | Gap vs Our Product |
|------------|-----------|------------|--------------------|
| **HackerRank** | Large DSA library, plagiarism detection, AI-assisted grading, ATS integrations, enterprise-ready | Expensive for startups, complex UI | ❌ No public job board; ❌ No automatic AI resume-to-interview pipeline; ❌ Limited authenticity questioning after submission |
| **Codility** | Strong anti-cheating, real-world coding tasks, automated scoring | No integrated hiring funnel, no resume AI matching | ❌ No end-to-end hiring pipeline; ❌ No student-first job discovery |
| **CodeSignal** | Modern UI, gamified scoring, live interview environment | Expensive, resume screening not core | ❌ No job board; ❌ No auto interview trigger |
| **HackerEarth** | Hackathons, budget-friendly, good coding library | UI not enterprise-grade, weak resume intelligence | ❌ No public job board; ❌ Limited AI resume screening |
| **iMocha** | AI interviewer bot, AI proctoring, skill-based assessments | No strong public job marketplace, less DSA depth vs HackerRank | ❌ No job board; ⚠️ Authenticity Q&A limited |

### 12.2 Competitive Feature Comparison Table

| Feature | Our Platform | HackerRank | Codility | CodeSignal | iMocha |
|---------|:------------:|:----------:|:--------:|:----------:|:------:|
| Public Job Board | ✅ | ❌ | ❌ | ❌ | ❌ |
| AI Resume Screening | ✅ | ⚠️ Limited | ❌ | ⚠️ | ⚠️ |
| Auto Interview Trigger | ✅ | ❌ | ❌ | ❌ | ❌ |
| DSA Interview | ✅ | ✅ | ✅ | ✅ | ⚠️ |
| Authenticity Q&A (post-code) | ✅ | ❌ | ❌ | ❌ | ⚠️ |
| Risk Score from Proctoring | ✅ | ⚠️ | ✅ | ⚠️ | ✅ |
| Full Hiring Pipeline | ⚠️ Future | ❌ | ❌ | ❌ | ❌ |
| Multi-tenant Companies | ✅ | ✅ | ✅ | ✅ | ✅ |

*Legend: ✅ Full support | ⚠️ Partial/Limited | ❌ Not available*

---

## 13. Business Model & Pricing Strategy

### 13.1 Pricing Models

| Model | Description | Target |
|-------|-------------|--------|
| **Model A – SaaS Subscription** | ₹X / month per recruiter; interview credits included | SMBs, startups |
| **Model B – Pay Per Interview** | ₹Y per interview completed; no monthly fee | Occasional hirers |
| **Model C – Enterprise License** | Annual contract; custom integrations, SSO, dedicated support | Large enterprises |

### 13.2 Revenue Add-ons
- Advanced analytics & dashboards
- Video proctoring (manual interview recording)
- ATS integrations (Greenhouse, Lever, Workday)
- White-labeled version
- Dedicated AI model customization

### 13.3 Cost Considerations
- Cost per AI interview (compute, AI API)
- Cost per resume scan
- Storage for recordings, logs, reports

---

## 14. Market Analysis

### 14.1 Target Customers
- **Startups & SMBs:** Cost-conscious; need efficient screening without large HR teams
- **Enterprises:** Need compliance, integrations, audit trails
- **Colleges/Placement Cells:** Bulk campus hiring; student-first job discovery

### 14.2 Market Sizing (To Be Defined)
| Metric | Description |
|--------|-------------|
| **TAM** | Total addressable market (all companies hiring tech talent) |
| **SAM** | Serviceable addressable market (companies using online assessments) |
| **SOM** | Serviceable obtainable market (realistic Year 1–3 target) |

### 14.3 Geography Focus
- **Phase 1:** India (colleges, startups, IT hiring)
- **Phase 2:** APAC, then global

---

## 15. Success Metrics & KPIs

| KPI | Target | Notes |
|-----|-------|------|
| Avg time-to-hire reduction | 30% | vs manual screening baseline |
| AI screening accuracy | 85%+ | Match/no-match vs human review |
| Interview completion rate | 90% | Started vs completed |
| Cheating detection accuracy | 80%+ | Risk score correlation with manual review |
| Candidate satisfaction score | 4+ / 5 | Post-interview survey |
| System uptime | 99.9% | For interview & job board |
| Resume scan time | &lt; 60 sec | P95 |
| API response time | &lt; 300 ms | P95 |

---

## 16. Risk & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| **AI bias in scoring** | Unfair outcomes, legal exposure | Bias monitoring, blind scoring option, recruiter override |
| **Legal issues with copy logging** | Privacy complaints, GDPR | Explicit consent, data retention policy, anonymization where possible |
| **Browser limitations for proctoring** | Cheating undetected | Document limitations; combine with authenticity Q&A |
| **Candidate backlash on surveillance** | Low adoption, bad reviews | Transparent disclosure, minimal necessary logging |
| **Question leakage** | Cheating, loss of trust | Large question pool, randomization, per-candidate variants |
| **AI model drift** | Inaccurate scores over time | Quarterly calibration, manual audit sample |
| **Failed resume parse / AI errors** | Poor candidate experience | Fallback to manual review, retry logic, error handling |

---

## 17. API & Integration Architecture

### 17.1 API-First Design
The platform exposes secure **REST APIs** for integration with:
- ATS systems (Greenhouse, Lever, Workday)
- HRMS platforms
- University/placement systems
- Enterprise dashboards & reporting tools

### 17.2 Core API Modules

| Module | Key Endpoints |
|--------|---------------|
| **Authentication** | `POST /auth/signup`, `POST /auth/login`, `POST /auth/invite`, `POST /auth/role-assign` |
| **Jobs** | `POST /jobs`, `GET /jobs`, `PATCH /jobs/{id}`, `DELETE /jobs/{id}` |
| **Candidates** | `POST /candidates/import`, `GET /candidates`, `GET /candidates/{id}`, `PATCH /candidate-status` |
| **Interview** | `POST /interview/invite`, `GET /interview/report/{candidateId}`, `POST /interview/reschedule` |
| **Reports** | `GET /reports/job/{jobId}`, `GET /reports/export/pdf`, `GET /reports/export/csv` |

### 17.3 Webhooks (Real-Time Sync)

| Event | Webhook Trigger |
|-------|-----------------|
| Candidate applied | `candidate.applied` |
| Resume shortlisted | `candidate.shortlisted` |
| Interview completed | `interview.completed` |
| Candidate passed | `candidate.passed` |
| Candidate failed | `candidate.failed` |

---

## 18. Enterprise Security & Compliance

### 18.1 Security Architecture
- HTTPS enforced everywhere
- Encryption at rest (AES-256)
- JWT-based authentication
- Role-Based Access Control (RBAC)
- Rate limiting & IP throttling
- Audit logging for sensitive actions

### 18.2 Compliance Roadmap

| Compliance | Status |
|------------|--------|
| GDPR readiness | Phase 1 |
| Data retention policy | Defined |
| Candidate consent logging | Mandatory |
| SOC 2 | Phase 2 |
| ISO 27001 | Planned |

### 18.3 Data Retention Policy

| Data Type | Retention | Configurable |
|-----------|-----------|--------------|
| Resume data | 12 months | Yes |
| Interview logs | 12 months | Yes |
| Proctoring logs | 6–12 months | Yes |
| Video (if enabled) | Per policy | Yes |

---

## 19. AI Governance & Transparency

### 19.1 AI Scoring Model (Default Weights)

| Factor | Weight | Description |
|--------|--------|-------------|
| Code Correctness | 40% | Test cases passed, logic correctness |
| Efficiency/Complexity | 20% | Time/space complexity, optimal approach |
| Test Case Coverage | 15% | Edge cases handled |
| Code Quality | 10% | Readability, structure |
| Explanation Quality | 10% | Follow-up Q&A on submitted code |
| Risk/Proctoring | 5% | Violations, copy events |

*Recruiters can configure weights per job.*

### 19.2 AI Validation Strategy
- 5–10% of interviews manually audited
- Bias detection monitoring
- Drift monitoring on model performance
- **Recruiter override capability** for final score
- Model retraining schedule (quarterly)

### 19.3 Anti-Bias Measures
- Blind resume scoring option (hide name, college)
- Optional removal of college/institution from scoring
- Equal difficulty question pools
- Audit trail for AI decisions

---

## 20. Scalability & Performance

### 20.1 Infrastructure Design
- Cloud-native deployment (AWS/GCP/Azure)
- Microservices architecture
- Auto-scaling containers
- CDN for job board
- Separate interview compute engine

### 20.2 Target Performance Metrics

| Metric | Target |
|--------|--------|
| Resume scan time | &lt; 60 sec |
| Interview launch time | &lt; 30 sec |
| Concurrent interviews | 5,000+ |
| System uptime | 99.9% |
| API response time | &lt; 300 ms |

### 20.3 Scalability Definition
- Max concurrent interviews supported: 5,000+
- Target companies (Year 1): TBD
- Target students per month: TBD
- Auto-scaling on demand for interview sessions

---

## 21. Analytics & Dashboard System

### 21.1 Recruiter Dashboard
- Total applicants per job
- Screening pass rate
- Interview pass rate
- Average score per job
- Risk score distribution
- Time-to-hire metrics

### 21.2 Candidate Analytics
- Performance breakdown (factor-wise)
- Historical attempts
- Improvement suggestions (student-facing)

### 21.3 Platform Admin Dashboard
- Companies onboarded
- Active interviews
- Revenue metrics
- AI accuracy tracking

---

## 22. Advanced Anti-Cheating Layer

Beyond current proctoring (full-screen, tab/screen switch, copy logging):

| Layer | Description |
|-------|-------------|
| **Behavioral monitoring** | Typing speed analysis, sudden paste detection, long inactivity flag |
| **Question randomization** | Large question pool, random test cases, per-candidate question variants |
| **Plagiarism engine** | Compare against internal DB, internet known solutions, previous candidates |

---

## 23. Strategic Positioning

### 23.1 Positioning Statement
*"The first AI-powered end-to-end hiring pipeline combining resume intelligence, authenticity-driven DSA interviews, and risk-scored proctoring."*

### 23.2 Category Definition
We are **not** competing only with HackerRank, Codility, CodeSignal, HackerEarth, or iMocha. We are defining a new category:

**AI Hiring Infrastructure** — Job board + AI screening + proctored DSA + authenticity checks + configurable pipeline.

### 23.3 Key Differentiators
- **Integrated workflow:** Job board → AI screening → auto interview invite → proctored DSA → report (competitors assume you import candidates)
- **Authenticity intelligence:** Post-code Q&A to verify candidate wrote and understands the solution
- **Student-first:** No signup to apply; one account for multiple companies
- **Extensible pipeline:** Future stages (MCQ, manual interview with GMeet, recording, pass/fail)

---

## 24. Glossary

| Term | Definition |
|------|------------|
| **Super Admin** | First user of a company; can invite members and assign Admin / Read-only. |
| **Admin** | Recruiter role with permission to post jobs, manage candidates, view reports. |
| **Read-only User** | Recruiter role with view-only access to jobs, candidates, reports. |
| **DSA** | Data Structures and Algorithms (e.g. arrays, trees, graphs, sorting, DP). |
| **Job board** | Public or gated list of jobs where students can apply. |
| **Risk factor** | Score or level derived from proctoring events (tab/screen switch, copy, inspect) indicating cheating likelihood. |
| **Factor (scoring)** | Dimension on which the student is rated (e.g. code correctness, complexity, communication, test cases). |
| **Bulk onboard** | Importing multiple students via CSV/Excel to create or link accounts. |
| **ATS** | Applicant Tracking System (e.g. Greenhouse, Lever, Workday). |
| **TAM/SAM/SOM** | Total / Serviceable Addressable / Serviceable Obtainable Market. |

---

**Document End**

*For questions or change requests, contact the Product Owner or document author.*
