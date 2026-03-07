# Workflows – End-to-End Guide

This document describes **full user journeys** and **which APIs to call in order**. Use it to implement screens and navigation.

---

## 1. Recruiter: First-time setup (Super Admin)

**Goal:** Create the first company and become its Super Admin.

1. **Sign up (first user only)**  
   - **API:** `POST /auth/signup-super-admin`  
   - **Body:** `{ "email", "password", "fullName", "companyName" }`  
   - **Response:** `accessToken`, `refreshToken`  
   - **Frontend:** Store tokens (e.g. in memory + refresh in http-only cookie or secure storage). Redirect to recruiter dashboard or company home.

2. **Get company ID for later calls**  
   - Either decode JWT (if companyId is in claims) or:  
   - **API:** `GET /companies/{id}` — you need the company `id`. After signup, the backend creates one company; you can get its id from the first company list or a dedicated “my companies” endpoint if added. For now, the signup response does not return companyId; you may need to call `GET /dashboards/recruiter?companyId=...` after the user selects or you store the first company id from another call (e.g. get company by slug or from `/me`-style endpoint if available).  
   - **Workaround:** Call `GET /dashboards/recruiter?companyId=<id>` with a known id, or implement a “default company” from login response. If the backend adds `GET /me` for recruiter returning companies, use that. Otherwise, the first company can be stored at first login (e.g. from a “welcome” endpoint or from list members of first company).  
   - **Practical:** After signup, redirect to a “select company” or “company home” screen; if the app has only one company per user initially, store that company id when you first fetch it (e.g. from company list or dashboard).

3. **Optional: Update branding**  
   - **API:** `PATCH /companies/{id}/branding`  
   - **Body:** `{ "brandingConfigJson": "{\"primary\":\"#hex\"}" }`

**APIs used:**  
`POST /auth/signup-super-admin` → (get company id from your app state or one extra call) → optional `PATCH /companies/{id}/branding`.

---

## 2. Recruiter: Login

**Goal:** Log in and get tokens.

1. **API:** `POST /auth/login`  
   - **Body:** `{ "email", "password" }`  
   - **Response:** `accessToken`, `refreshToken`  
   - Store tokens; redirect to recruiter dashboard. Use `companyId` from your stored context or from a “default company” / first company fetch.

2. **Token refresh (when you get 401/403 due to expired token)**  
   - **API:** `POST /auth/refresh`  
   - **Body:** `{ "refreshToken": "<stored_refresh_token>" }`  
   - **Response:** New `accessToken`, `refreshToken`. Replace stored tokens and retry the failed request.

**APIs used:**  
`POST /auth/login`; on expiry: `POST /auth/refresh`.

---

## 3. Recruiter: Invite team member

**Goal:** Invite another user to the company; they accept via link.

1. **Recruiter (SuperAdmin/Admin)**  
   - **API:** `POST /auth/invite`  
   - **Body:** `{ "companyId", "email", "roleName" }` (e.g. `roleName`: `"Admin"`, `"ReadOnly"`)  
   - **Response:** `inviteToken`, `expiresAt`  
   - **Frontend:** Build invite link: e.g. `https://app.example.com/accept-invite?token=<inviteToken>` and send (email or copy link).

2. **Invitee (opens link)**  
   - **API:** `POST /auth/accept-invite`  
   - **Body:** `{ "token": "<inviteToken>", "password": "<chosen_password>" }`  
   - **Response:** `accessToken`, `refreshToken`  
   - **Frontend:** Store tokens; redirect to dashboard or company home.

**APIs used:**  
`POST /auth/invite` → (send link) → user: `POST /auth/accept-invite`.

---

## 4. Recruiter: Company and pipelines

**Goal:** View company, members, and pipelines; optionally update branding.

1. **Get company**  
   - **API:** `GET /companies/{id}`  
   - Use for header, name, branding.

2. **List members**  
   - **API:** `GET /companies/{id}/members`  
   - Show table: email, role, status.

3. **List pipelines**  
   - **API:** `GET /companies/{companyId}/pipelines`  
   - **Response:** List of `{ "id", "companyId", "name", "default" }`.

4. **Get pipeline stages (e.g. for dropdown or config)**  
   - **API:** `GET /pipelines/{pipelineId}/stages`  
   - **Response:** Ordered list of stages (e.g. resume_screening, ai_interview, offer).

5. **Update branding (optional)**  
   - **API:** `PATCH /companies/{id}/branding`  
   - **Body:** `{ "brandingConfigJson": "..." }`

**APIs used:**  
`GET /companies/{id}`, `GET /companies/{id}/members`, `GET /companies/{companyId}/pipelines`, `GET /pipelines/{pipelineId}/stages`, optional `PATCH /companies/{id}/branding`.

---

## 5. Recruiter: Job lifecycle

**Goal:** Create, list, edit, and delete jobs.

1. **Create job**  
   - **API:** `POST /jobs`  
   - **Body:** `{ "companyId", "title", "description", "location", "employmentType", "applicationDeadline", "maxApplications", "resumeCriteriaJson", "customFormSchemaJson", "scoringWeightsOverrideJson", "pipelineId" }` (only `companyId` and `title` required).  
   - **Response:** Job object with `id`. Store `id` for next steps.

2. **List jobs (recruiter)**  
   - **API:** `GET /jobs?companyId=<uuid>&page=0&size=20`  
   - **Response:** Paginated: `content`, `totalElements`, `totalPages`, etc. Use for job list table.

3. **Get one job**  
   - **API:** `GET /jobs/{id}`  
   - Use for job detail / edit form.

4. **Update job**  
   - **API:** `PUT /jobs/{id}`  
   - **Body:** Same fields as create (partial update).

5. **Delete job**  
   - **API:** `DELETE /jobs/{id}`  
   - **Response:** `204 No Content`.

**APIs used:**  
`POST /jobs`, `GET /jobs?companyId=`, `GET /jobs/{id}`, `PUT /jobs/{id}`, `DELETE /jobs/{id}`.

---

## 6. Candidate: Public job board and apply

**Goal:** Browse jobs and submit an application (no login required to apply).

1. **List public jobs**  
   - **API:** `GET /jobs/public`  
   - **Response:** Array of jobs. Show as job board.

2. **Apply to a job**  
   - **API:** `POST /jobs/{jobId}/apply`  
   - **Body:** `{ "email", "fullName", "phone", "resumeStoragePath", "resumeOriginalFilename", "answers" }`  
   - **Note:** Resume file must be uploaded first if your backend exposes an upload endpoint; otherwise `resumeStoragePath` / `resumeOriginalFilename` may come from a separate upload flow or placeholder.  
   - **Response:** Application object. Show confirmation and optionally “Track application” (if candidate later logs in and uses `/me/applications`).

**APIs used:**  
`GET /jobs/public`, `POST /jobs/{jobId}/apply`.

---

## 7. Candidate: Profile and saved jobs (logged in)

**Goal:** View profile, my applications, and saved jobs; add/remove saved job.

1. **Get my profile**  
   - **API:** `GET /me`  
   - **Response:** Candidate profile (e.g. fullName, email, phone). Only for `userType === "candidate"`.

2. **List my applications**  
   - **API:** `GET /me/applications`  
   - Use for “My applications” list with status.

3. **List saved jobs**  
   - **API:** `GET /me/saved-jobs`  
   - Show saved job cards with link to job (use job id to open job detail or public job view).

4. **Save a job**  
   - **API:** `POST /me/saved-jobs`  
   - **Body:** `{ "jobId": "uuid" }`

5. **Unsave a job**  
   - **API:** `DELETE /me/saved-jobs/{id}`  
   - `id` = saved job record id (from list).

**APIs used:**  
`GET /me`, `GET /me/applications`, `GET /me/saved-jobs`, `POST /me/saved-jobs`, `DELETE /me/saved-jobs/{id}`.

---

## 8. Recruiter: View applications and stage progress

**Goal:** Open an application and see pipeline stage progress.

1. **Get application**  
   - **API:** `GET /applications/{id}`  
   - **Response:** Application details (candidate, job, status, etc.). Recruiter must be member of the job’s company.

2. **Get stage progress**  
   - **API:** `GET /applications/{id}/stage`  
   - **Response:** List of stages with status (e.g. pending, in_progress, passed, failed). Use for pipeline view or stepper.

**APIs used:**  
`GET /applications/{id}`, `GET /applications/{id}/stage`.

---

## 9. Scheduling: Invitation link → Book slot

**Goal:** Candidate receives link with token; opens it and books an interview slot.

1. **Get invitation by token (from link)**  
   - **API:** `GET /interview-invitations/{token}`  
   - **Response:** Invitation info (application, job, interview type, etc.). Use to show job title and slot picker.

2. **Book slot**  
   - **API:** `POST /interview-invitations/{token}/slots`  
   - **Body:** `{ "scheduledStartAt": "ISO8601", "scheduledEndAt": "ISO8601" }`  
   - **Response:** Slot confirmation. Then show “Start interview” when the time comes, using the same `token` for the next workflow.

**APIs used:**  
`GET /interview-invitations/{token}`, `POST /interview-invitations/{token}/slots`.

---

## 10. Interview: Start → Questions → Submit code → Follow-ups

**Goal:** Candidate starts interview, gets questions, submits code, answers follow-ups.

1. **Start interview**  
   - **API:** `POST /interviews/start`  
   - **Body:** `{ "invitationToken": "<token>" }`  
   - **Response:** `interviewId`, `firstQuestionId`, `firstQuestionTitle`, `firstQuestionDescription`, `firstQuestionStarterCode`  
   - **Frontend:** Store `interviewId`; show first question and code editor; optionally start proctoring (next workflow).

2. **Get interview (optional)**  
   - **API:** `GET /interviews/{id}`  
   - **Response:** `id`, `applicationId`, `type`, `status`, `startedAt`, `endedAt`. Use for status or resume.

3. **Submit code for a question**  
   - **API:** `POST /interviews/{interviewId}/questions/{interviewQuestionId}/submit-code`  
   - **Body:** `{ "language", "code" }`  
   - **Response:** `{ "status", "interviewQuestionId" }`. Then show next question or follow-ups (from response or next GET if needed).

4. **Answer follow-up**  
   - **API:** `POST /interviews/{interviewId}/followups/{followupQuestionId}/answer`  
   - **Body:** `{ "answerText": "string" }`  
   - **Response:** `{ "status", "followupQuestionId" }`. Continue until all questions/discussions are done.

**APIs used:**  
`POST /interviews/start`, `GET /interviews/{id}`, `POST /interviews/.../questions/.../submit-code`, `POST /interviews/.../followups/.../answer`.

---

## 11. Proctoring: Start session → Send events → End

**Goal:** Record proctoring events during the interview.

1. **Start proctoring session**  
   - **API:** `POST /proctoring/sessions`  
   - **Body:** `{ "interviewId": "uuid" }`  
   - **Response:** Session object with `id`. Store `sessionId`.

2. **Send events (tab switch, fullscreen exit, etc.)**  
   - **API:** `POST /proctoring/sessions/{sessionId}/events`  
   - **Body:** `{ "eventType": "string", "detailsJson": "string", "weight": number }`  
   - Call repeatedly as events occur (throttle if needed; backend rate-limits).

3. **End session**  
   - **API:** `POST /proctoring/sessions/{sessionId}/end`  
   - Call when interview ends or user leaves.

**APIs used:**  
`POST /proctoring/sessions`, `POST /proctoring/sessions/{sessionId}/events`, `POST /proctoring/sessions/{sessionId}/end`.

---

## 12. Recruiter: Reports and export

**Goal:** View report for an application, job summary, and export CSV.

1. **Report for application**  
   - **API:** `GET /reports/applications/{applicationId}`  
   - **Response:** Report (scores, factors, risk, etc.). Show on application/report page.

2. **Summary for job**  
   - **API:** `GET /reports/jobs/{jobId}`  
   - **Response:** e.g. `totalApplications`, `jobTitle`. Use for job dashboard or header.

3. **Export CSV**  
   - **API:** `GET /reports/export?jobId=<uuid>` or `?applicationId=<uuid>`  
   - **Response:** CSV file (binary/stream). Trigger download in browser (e.g. use response blob and download link).

**APIs used:**  
`GET /reports/applications/{applicationId}`, `GET /reports/jobs/{jobId}`, `GET /reports/export?jobId=...`.

---

## 13. Recruiter: Dashboard and bulk import

**Goal:** See recruiter stats; list bulk import batches.

1. **Recruiter dashboard**  
   - **API:** `GET /dashboards/recruiter?companyId=`  
   - **Response:** `companyId`, `totalJobs`, `totalApplications`. Show on dashboard home.

2. **Bulk import batches**  
   - **API:** `GET /bulk-import/batches?companyId=`  
   - **Response:** List of batches (id, status, sourceFilePath, createdAt).

3. **Bulk import batch detail**  
   - **API:** `GET /bulk-import/batches/{id}`  
   - **Response:** Batch + `candidateCount`.

**APIs used:**  
`GET /dashboards/recruiter?companyId=`, `GET /bulk-import/batches?companyId=`, `GET /bulk-import/batches/{id}`.

---

## 14. Platform admin and audit

**Goal:** Platform admin sees global stats; company members see audit logs.

1. **Platform admin dashboard**  
   - **API:** `GET /dashboards/platform-admin`  
   - **Auth:** User must have `userType === "platform_admin"`.  
   - **Response:** e.g. `totalCompanies`, `totalUsers`.

2. **Audit logs (company)**  
   - **API:** `GET /audit-logs?companyId=&page=0&size=20`  
   - **Response:** Paginated audit entries. Show in audit log table.

**APIs used:**  
`GET /dashboards/platform-admin`, `GET /audit-logs?companyId=`.

---

## 15. Password reset and verify email

**Goal:** User requests reset; user resets password or verifies email via token.

1. **Request password reset**  
   - **API:** `POST /auth/request-password-reset`  
   - **Body:** `{ "email" }`  
   - **Frontend:** Show “Check your email” message.

2. **Reset password (from email link)**  
   - **API:** `POST /auth/reset-password`  
   - **Body:** `{ "token", "newPassword" }`  
   - **Frontend:** Form with token (from query param) and new password.

3. **Verify email**  
   - **API:** `POST /auth/verify-email`  
   - **Body:** `{ "token" }`  
   - Use from email link.

**APIs used:**  
`POST /auth/request-password-reset`, `POST /auth/reset-password`, `POST /auth/verify-email`.

---

## 16. Rate limiting (429)

**When:** Any request can return **429 Too Many Requests** (auth, interview, proctoring are rate-limited per IP/user).

**Frontend:**  
- Show a friendly message: “Too many requests. Please try again in a minute.”  
- Optionally disable the button or form for 60 seconds and then allow retry.  
- Do not retry in a tight loop.

---

## 17. Suggested order to implement

1. **Auth:** Login, signup (super-admin), refresh, logout.  
2. **Company:** Get company, list members, dashboard (recruiter).  
3. **Jobs:** List (by company), create, get, update, delete; public list.  
4. **Apply:** Public job board + apply form.  
5. **Applications:** Get application, get stage (recruiter and candidate).  
6. **Candidate /me:** Profile, applications, saved jobs.  
7. **Scheduling:** Invitation by token, book slot.  
8. **Interview:** Start, get, submit-code, follow-up answer.  
9. **Proctoring:** Start session, events, end.  
10. **Reports:** By application, by job, export CSV.  
11. **Invite/accept-invite, password reset, verify email.**  
12. **Bulk import, pipelines, notifications/webhooks, audit, platform admin.**

Use [API-REFERENCE.md](API-REFERENCE.md) for exact request/response shapes and errors.
