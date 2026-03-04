---
name: project-db-design-doc
overview: Create a database design document in the project folder for the Recruitment & AI Interview Platform, based on the approved BRD and existing schema plan.
todos:
  - id: write-db-doc-structure
    content: Create a db-design markdown file in the project folder with sections for approach, core tables, indexes, ER diagram, and extensibility notes.
    status: completed
isProject: false
---

## Goal

- Create a concise, implementation-ready **DB design markdown document** inside the project folder (e.g. `[c:/Users/aryan/Desktop/Future Scope]/db-design.md`) that your team can read without looking at internal plan files.

## Document Structure

- **Title & Scope**
  - Add a title (e.g. "Recruitment & AI Interview Platform – Database Design").
  - Briefly describe scope: PostgreSQL-based, multi-tenant, aligned with `BRD-Recruitment-AI-Interview-Platform.md`.
- **High-Level Approach**
  - Summarize choices from the plan: single `users` table with `user_type`, multi-tenancy via `company_id`, JSONB for flexible configs (resume criteria, forms, weights), and strict foreign keys.
- **Core Entity Tables**
  - Document tables and key fields in grouped sections, mirroring the plan file:
    - Auth & RBAC: `users`, `companies`, `roles`, `company_members`, `user_sessions`, `email_verifications`, `password_resets`, `mfa_methods`.
    - Student data: `student_profiles`, `resumes`, `saved_jobs`, `bulk_import_batches`, `bulk_import_students`.
    - Jobs & applications: `jobs`, `job_custom_fields`, `job_applications`, `application_answers`.
    - Pipelines: `pipelines`, `pipeline_stages`, `application_stage_progress`.
    - AI resume screening & scheduling: `resume_screenings`, `interview_invitations`, `interview_slots`.
    - Interview engine: `question_bank_questions`, `question_test_cases`, `interviews`, `interview_questions`, `code_submissions`, `followup_questions`, `followup_answers`, `manual_interviews`.
    - Proctoring: `proctoring_sessions`, `proctoring_events`, `behavioral_metrics`.
    - Reporting & integrations: `interview_reports`, `interview_report_factors`, `notifications`, `webhook_events`, `audit_logs`.
  - For each group, list table names with 1–2 line descriptions and only the most important columns (PKs, main FKs, key JSONB fields).
- **Indexes & Performance Notes**
  - Call out critical indexes:
    - Tenant and listing filters: `company_id`, `is_published`, `application_deadline` on `jobs`.
    - Funnel queries: (`job_id`, `status`) on `job_applications`, (`application_id`) on `interviews`, `interview_reports`, `resume_screenings`.
    - User and auth flows: unique `email` on `users`, `user_id` on `user_sessions`.
  - Mention that additional indexes will be added pragmatically based on query patterns.
- **Mermaid ER Diagram**
  - Embed the ER diagram from the plan using a `mermaid` code block to visualize relationships, ensuring IDs use names like `CompanyMember`, `StudentProfile`, `JobApplication` (no spaces in node IDs).
- **Extensibility & Future Scope**
  - Briefly describe how future stages (MCQ rounds, manual interviews, advanced anti-cheating, ATS integrations) are supported via:
    - `pipelines` / `pipeline_stages`.
    - `manual_interviews`.
    - `behavioral_metrics`.
    - `webhook_events` and `notifications`.

## Implementation Steps (when you exit Plan mode)

- Create a new markdown file in the project root, e.g. `[c:/Users/aryan/Desktop/Future Scope]/db-design.md`.
- Copy the agreed table list and ER diagram from the plan file into clear sections as described above.
- Optionally, append a final section with a link or reference to the actual migration/DDL scripts once those are generated.
