# Functional & Non-Functional Requirements

The BRD (version 1.0, March 2026) outlines a comprehensive **AI-powered recruitment platform** focused on automated technical (DSA) interviews.  Key **functional requirements** include:

- **Authentication & User Management** – Multi-tenant support with roles (Super Admin, Admin, Read-only); company onboarding; email invites; single student account across companies【9†L1-L4】.  
- **Recruiter Features** – Company setup, job posting (title, description, criteria), bulk student import (CSV/Excel) and applicant tracking with statuses (applied, screened, etc.)【9†L1-L4】.  
- **Job Board & Application** – Public job board (no login needed to view), application form with resume upload, automatic student account creation on first apply, multi-apply support【9†L1-L4】.  
- **AI Resume Screening** – Automated parsing of uploaded resumes against recruiter-defined criteria; invitations to AI interview if criteria match, or flag “under review” otherwise【9†L1-L4】.  
- **AI Interview Engine** – Two sequential coding questions (DSA); AI evaluation of code submission (correctness, complexity, style, test results) plus follow-up interview questions【9†L1-L4】. The system generates a *factor-based report* (score out of 100) on dimensions like correctness, efficiency, explanation, and test-case coverage.  
- **Proctoring & Anti-Cheating** – Full-screen enforced interview window; detection/alert on screen/tab switches; disabled dev tools; logging of copy/paste events; and aggregation of violations into a “risk score” in the final report【9†L1-L4】.  
- **Reporting** – Detailed candidate reports for recruiters (overall score, factor breakdown, code results, proctoring summary) with export capability【9†L1-L4】.  

The **non-functional requirements** specify performance, reliability and security standards: e.g. resume scanning and interview startup in <2 minutes (SLA), resume of disconnected sessions, secure handling of links and tokens (HTTPS, encryption), data privacy for PII (e.g. resume data) under retention policies, browser compatibility (Chrome/Edge/Firefox), and explicit consent for proctoring features【9†L1-L4】. These cover **security**, **compliance**, and **usability** aspects. 

Stakeholders (Product, Engineering, Recruiters, HR) drove goals like reducing time-to-hire, improving hire quality, scalability, fairness/transparency, and candidate experience (no login barrier for applicants)【9†L1-L4】. The BRD’s *Unique Selling Propositions* emphasize AI-first screening, authenticity checks (self-explanation), end-to-end proctoring, structured DSA interviews, and frictionless student onboarding. Overall, the BRD is detailed: it defines user flows (recruiter signup-to-report, student apply-to-report) and system components (modules for auth, jobs, applications, AI services, proctoring, scoring) that align with these requirements.

# Stakeholders & User Personas

The BRD identifies key **personas** and roles: 

- **Recruiters:** Super Admin, Admin, and Read-Only recruiter users. Super Admin onboards the company, invites team members, and can post jobs; Admins manage jobs/candidates and view reports; Read-Only can view data only. 
- **Students/Candidates:** Job seekers who browse the board, apply (triggering account creation), attend AI interviews, and receive feedback. 
- **System/AI:** The automated system that scans resumes, schedules interviews, conducts proctored coding sessions, evaluates code, and generates reports.

These cover the core stakeholders: hiring managers/teams and applicants. The **Goals & Objectives** map to these personas: e.g. recruiters need efficiency and data (time-to-hire, quality), candidates need a clear process, and the product must support multi-company tenants and fairness (clear rules, no hidden monitoring).

# Market & Competitor Analysis

Existing **coding interview and assessment platforms** offer many overlapping features. For example, *HackerRank*, *Codility*, *CodeSignal*, *HackerEarth*, *Coderbyte*, and *iMocha* are prominent solutions. They all provide coding challenges, real-time coding environments, and some form of proctoring or anti-cheating. Comparing features:

- **HackerRank** – Extensive question library (40+ languages) and live coding interviews. It offers automated proctoring and plagiarism detection to ensure fairness. Its “take-home assessments” run in a secure environment with thousands of expert-designed challenges【6†L153-L161】【7†L151-L158】. (However, reports note high pricing and an older UI【5†L18-L21】.)
- **Codility** – Focuses on real-world coding tasks and strong anti-cheating measures. It includes **automated scoring** and built-in **plagiarism detection**【7†L259-L262】. (Drawbacks: test creation can be time-consuming【5†L18-L21】.)
- **CodeSignal** – Offers realistic coding interview simulations and live collaborative sessions with modern UI. Supports a rich interview environment but can be complex/expensive【5†L18-L21】.
- **HackerEarth** – Includes hackathon support and budget-friendly pricing【3†L124-L130】. Provides coding assessments but is noted to have a less polished UI【3†L124-L130】. It emphasizes community hackathons.
- **Coderbyte** – A simpler, lower-cost platform for coding assessments【3†L126-L130】. It offers many challenges but fewer advanced features (suitable for early-stage screening).
- **CoderPad/CodeInterview/Devskiller** – Emphasize *collaborative live coding*: interactive interview editors, code execution and playback, audio/video. For instance, CoderPad supports 30+ languages with code playback【7†L180-L189】; CodeInterview adds audio/video and question banks【7†L201-L210】. These tools excel in live interviews but do not always automate scoring or AI screening.

- **iMocha** – An emerging platform with AI-based features. It provides **AI proctoring** (webcam monitoring, browser tracking, fraud detection) and an “AI interviewer” bot to conduct first-round screens【7†L116-L124】. It also offers coding simulators and live coding options. 

**Benchmarking against the BRD**: Many competitors cover coding assessment and proctoring (e.g. HackerRank, iMocha, Codility【7†L151-L158】【7†L259-L262】). However, *few combine a public job board and automated scheduling* with coding assessments as an integrated workflow. The BRD’s pipeline (job board → AI screening → interview scheduling → proctoring) is more end-to-end than a standalone testing tool. For example, HackerRank and Codility focus on providing tests and anti-cheat, but they assume you import candidates or send invites – they don’t natively host a multi-company job board. Similarly, live-coding tools like CoderPad streamline the interview itself but do not handle resume screening or job postings. 

**Key competitor points from sources:** HackerRank advertises “AI-powered plagiarism detection” for fairness【9†L1-L4】. Codility “includes automated scoring and plagiarism detection”【7†L259-L262】. iMocha offers built-in proctoring with fraud detection and an AI interviewer bot【7†L118-L126】. These highlight the industry emphasis on test integrity and analytics. The BRD’s detailed proctoring (tab switch alerts, copy-log, risk scoring) aligns with this trend. 

# Gap Analysis & Missing Data

While the BRD is thorough, several **gaps and missing elements** are apparent:

- **Scalability & Performance Details:** The goals mention scalability, but there is no explicit architecture or load target. Non-functional requirements cite an example SLA (resume scan <2 min), but no measurable targets for concurrent users or system throughput. A plan for horizontal scaling or cloud infrastructure is not detailed. 
- **Metrics & Analytics:** Recruiters often need dashboards (e.g. total applicants, pass rates, average scores). The BRD only mentions individual report export. The “Future Scope” lists analytics dashboards (under 9.5 possible), but currently this is missing. 
- **Multilingual/Localization:** The platform appears English-only. Competitors often support multiple languages or local compliance (e.g. GDPR, CCPA specifics). There is a possible feature “Multi-language UI” mentioned, but no active plan in core scope. 
- **Identity Verification:** The BRD assumes a single student account per email, but does not address verifying identity. Some platforms add 2FA or ID upload to prevent proxy test-taking. This could be a risk if candidates share accounts. 
- **Candidate Experience Details:** No mention of feedback to students beyond the score. Does the system provide personalized feedback or tips (competitors like Coderbyte do). 
- **Accessibility/Compliance:** WCAG or other accessibility requirements are not in scope. Competitors often consider this for broad user bases. 
- **ATS/HR Integration:** While the BRD excludes full HRIS integration, modern tools often at least export results to common ATS or email. The “possible features” mention APIs and integrations, but core scope lacks it. 
- **Mobile Support:** The assumption is desktop for interviews. Mobile-friendly interfaces (especially for the job board or candidate portal) are not covered. 
- **Data & Metrics Missing:** There’s no mention of logging/audit trails (aside from possible “audit log” feature), or data retention periods. These details (how long to store interview videos, logs, etc.) are not specified. 
- **Legal & Privacy Depth:** NFR covers privacy generally, but specifics (e.g. GDPR data subject rights, record retention policies, encryption at rest) are missing. Only a note on “terms consent for proctoring”. 
- **AI Model Validation:** No mention of how the AI scoring is validated or calibrated. There is a constraint that AI may need calibration, but no strategy for it. 
- **Error Handling & Edge Cases:** How to handle failed jobs (e.g. if AI fails to parse a resume) or appeals (re-evaluation requests) is not covered. 
- **Localization of Time Zones:** Time slot selection is mentioned but no detail on time zone handling, which is critical in scheduling interviews across geographies. 
- **Question Bank Management:** The plan includes DSA questions, but lacks detail on how questions are authored, versioned, or randomized to prevent reuse.  
- **Candidate Support:** No mention of help or support for candidates (FAQs, chat, etc.) in case of technical issues.

Overall, the **missing data** mostly concerns *scale, integration, compliance,* and *usability enhancements*. 

# Improvement Opportunities & Recommendations

Based on the above analysis and industry best practices, we recommend the following improvements, prioritized as an **implementation roadmap**:

1. **Enhance Analytics & Reporting:** Quickly add recruiter dashboards showing aggregate metrics (applicants by stage, average scores, pass rates). This addresses a major recruiting need and is standard among competitors【3†L133-L142】【7†L165-L173】. Also, implement audit logs for compliance (tracking job posts, invites, report views). 
2. **Strengthen Security & Compliance:** Define data retention/archival policies explicitly. Use encryption at rest for PII. Implement identity verification (e.g. email OTP or ID upload) to ensure candidate authenticity. Clarify GDPR/CCPA compliance (users’ rights). 
3. **Add Integrations & Notifications:** Implement calendar sync/email reminders for interview slots (noted as future feature) and basic ATS export (CSV/API) so recruiters can link to existing systems. Also enable Single Sign-On (SSO) for enterprise recruiters (listed as possible).  
4. **Improve Candidate Experience:** Provide candidates with their report and some constructive feedback (e.g. “your solution could be optimized in X way”). A student-facing report (as future feature) will enhance transparency. Ensure UI is mobile-responsive at least for job applications. Competitors often highlight **candidate fairness and experience**【6†L153-L161】; matching this will increase adoption. 
5. **Expand Assessment Types:** The pipeline could allow customized interview content (adding behavioral or system-design questions as the future scope hints) to accommodate different roles. Build a question bank management system (tag questions by topic/role) and consider randomizing or versioning questions to prevent reuse (an anti-cheating measure). 
6. **AI Model Validation:** Plan for ongoing calibration of the AI scoring (e.g. manual review of a sample of interviews to check score accuracy and reduce bias). Document model accuracy targets. This aligns with the constraint noted that human review may be needed. 
7. **Scalability & Performance Planning:** Define performance targets (e.g. X interviews or resume scans per hour) and ensure the architecture (cloud services, auto-scaling) can meet them. Stress-test the full-screen interview components, as heavy proctoring may consume bandwidth/CPU. 
8. **Accessibility & Localization:** Begin internationalization (UI translation) if targeting global campuses. Ensure the UI meets WCAG standards (accessibility) so that all candidates can participate. This broadens the market and aligns with emerging legal requirements. 
9. **Mobile Support:** Although interviews require desktops, the job board and scheduling should work smoothly on mobile (many candidates apply via phones). Consider a basic mobile-responsive design for non-critical flows. 
10. **Future Pipeline Features:** As noted in the BRD, extending to MCQ rounds or human interviews will be valuable. When implementing, ensure the transition between stages (pass/fail rules) is configurable. Also add the ability for recruiters to record evaluations from human interviews (per 10.2 of BRD). 

Each of these steps should be prioritized based on business impact and development effort. For example, adding dashboards and basic integrations (steps 1-3) can often be done in parallel and would significantly improve the product’s value. Security/compliance (step 2) is critical before launch. Steps like expanding assessment types (5) and mobile support (9) follow, while full pipeline extensions (10) and advanced features can come later. 

# Competitors & Market Positioning

We should continuously monitor and learn from competitors. For example, HackerRank’s focus on **assessment fairness** and Codility’s emphasis on **anti-cheat analytics**【7†L259-L262】【6†L153-L161】 are best practices to uphold. Differentiators for our platform include: 

- **Integrated Hiring Workflow:** Unlike standalone code-testing tools, our platform combines a job board, AI screening, scheduling, proctoring, and reporting in one flow. This “end-to-end” pipeline (from application to results) can be a key selling point. 
- **Authenticity Checks:** The BRD’s “code explanation” step and detailed proctoring give an edge in preventing proxy cheating. Emphasizing this can appeal to employers concerned about exam integrity. 
- **Frictionless Candidate Experience:** Our design of no-login-first application lowers barriers. Competitors often require account creation upfront, so this could improve candidate conversion. 
- **Future Flexibility:** The roadmap to a configurable pipeline (MCQs, multiple DSA rounds, human interviews) will eventually match or exceed many competitor offerings. 

All enhancements should be framed as meeting or exceeding market standards. For instance, adding live coding (Pair Programming) interviews or AI assistants (like iMocha’s “AI interviewer”) could be future steps, as these are becoming popular【7†L118-L126】. 

# Conclusion

The BRD lays a solid foundation for an AI-driven coding interview platform. It clearly defines functional scopes (authentication, job management, AI interviews, proctoring) and non-functional needs (performance, security). To improve it, we recommend adding explicit requirements for scalability, analytics, integrations, and candidate support. Benchmarking shows that many competitors emphasize anti-cheating and reporting【7†L151-L158】【7†L259-L262】, so our platform should match these with robust security/proctoring. Implementing the prioritized features above – dashboards, integration hooks, candidate feedback, and compliance measures – will strengthen the product. Over time, expanding to multi-stage pipelines and richer analytics will keep us competitive. This roadmap ensures the final solution meets stakeholder goals while aligning with industry best practices.  

**Sources:** We reviewed the provided BRD and industry resources for competitor features. For example, HackerRank and Codility documentation highlight code assessment and anti-cheating features【6†L153-L161】【7†L259-L262】, and market comparisons underline the strengths/weaknesses of platforms like HackerRank, CodeSignal, and HackerEarth【5†L18-L21】【3†L124-L130】. These insights informed our analysis and recommendations.