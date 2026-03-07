# Feature Start Guide – Frontend

Use this as a **starting checklist** to implement the frontend. Each feature points to the **workflows** and **APIs** to use.

---

## Before you code

1. **Backend running** at `http://localhost:8080` (see [backend/README.md](../backend/README.md)).
2. **Read** [WORKFLOWS.md](WORKFLOWS.md) for end-to-end flows.
3. **Use** [API-REFERENCE.md](API-REFERENCE.md) for every endpoint (method, path, body, response).
4. **OpenAPI:** `http://localhost:8080/swagger-ui.html` for live API docs.

---

## Features to implement (in suggested order)

### 1. Auth & session

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Super-admin signup | §1 Recruiter first-time setup | `POST /auth/signup-super-admin` |
| Login | §2 Recruiter login | `POST /auth/login`, `POST /auth/refresh` |
| Logout | §2 | `POST /auth/logout` |
| Invite member | §3 Invite team member | `POST /auth/invite`, build link with `inviteToken` |
| Accept invite | §3 | `POST /auth/accept-invite` (from link) |
| Password reset | §15 | `POST /auth/request-password-reset`, `POST /auth/reset-password` |
| Verify email | §15 | `POST /auth/verify-email` |

**How to use:** Store `accessToken` and `refreshToken`; send `Authorization: Bearer <accessToken>` on every authenticated request. On 401, call `POST /auth/refresh` and retry.

---

### 2. Company & dashboard

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Company profile | §4 Company and pipelines | `GET /companies/{id}` |
| Company members | §4 | `GET /companies/{id}/members` |
| Update branding | §4 | `PATCH /companies/{id}/branding` |
| Recruiter dashboard | §13 | `GET /dashboards/recruiter?companyId=` |
| Pipelines list | §4 | `GET /companies/{companyId}/pipelines` |
| Pipeline stages | §4 | `GET /pipelines/{pipelineId}/stages` |

**How to use:** After login, you need a `companyId` (from context or first company). Use it for dashboard, jobs list, bulk import, audit.

---

### 3. Jobs (recruiter)

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Create job | §5 Job lifecycle | `POST /jobs` (body: companyId, title, + optional fields) |
| List jobs | §5 | `GET /jobs?companyId=&page=&size=` |
| Job detail | §5 | `GET /jobs/{id}` |
| Update job | §5 | `PUT /jobs/{id}` |
| Delete job | §5 | `DELETE /jobs/{id}` |

**How to use:** All require auth and company membership. Pagination: use `page`, `size`; response has `content`, `totalElements`, `totalPages`.

---

### 4. Job board & apply (candidate / public)

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Public job list | §6 Public job board | `GET /jobs/public` (no auth) |
| Apply to job | §6 | `POST /jobs/{jobId}/apply` (body: email, fullName, phone, resume*, answers) |

**How to use:** Apply is public. Resume: use `resumeStoragePath` and `resumeOriginalFilename` (upload flow if backend provides one).

---

### 5. Applications & stage progress

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Application detail | §8 View applications | `GET /applications/{id}` |
| Stage progress | §8 | `GET /applications/{id}/stage` |

**How to use:** Recruiter: for any application in their company. Candidate: only their own applications.

---

### 6. Candidate profile & saved jobs

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| My profile | §7 Candidate profile | `GET /me` (candidate only) |
| My applications | §7 | `GET /me/applications` |
| Saved jobs list | §7 | `GET /me/saved-jobs` |
| Save job | §7 | `POST /me/saved-jobs` (body: jobId) |
| Unsave job | §7 | `DELETE /me/saved-jobs/{id}` |

**How to use:** All require auth as candidate. Use saved job `id` from list for delete.

---

### 7. Scheduling (invitation → slot)

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Invitation page (by token) | §9 Scheduling | `GET /interview-invitations/{token}` |
| Book slot | §9 | `POST /interview-invitations/{token}/slots` (body: scheduledStartAt, scheduledEndAt) |

**How to use:** Token comes from URL (e.g. `/accept-invite/:token` or query). After booking, show “Start interview” when ready; use same token for interview start.

---

### 8. Interview (start → code → follow-ups)

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Start interview | §10 Interview | `POST /interviews/start` (body: invitationToken) |
| Get interview | §10 | `GET /interviews/{id}` |
| Submit code | §10 | `POST /interviews/{interviewId}/questions/{interviewQuestionId}/submit-code` (body: language, code) |
| Answer follow-up | §10 | `POST /interviews/{interviewId}/followups/{followupQuestionId}/answer` (body: answerText) |

**How to use:** After start, store `interviewId` and `firstQuestionId`. For each question, submit code then answer follow-ups as returned. Handle 429 (rate limit).

---

### 9. Proctoring

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Start proctoring | §11 Proctoring | `POST /proctoring/sessions` (body: interviewId) |
| Send events | §11 | `POST /proctoring/sessions/{sessionId}/events` (body: eventType, detailsJson, weight) |
| End session | §11 | `POST /proctoring/sessions/{sessionId}/end` |

**How to use:** Start when interview starts; send events on tab switch, fullscreen exit, etc.; end when interview ends.

---

### 10. Reports & export

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Application report | §12 Reports | `GET /reports/applications/{applicationId}` |
| Job summary | §12 | `GET /reports/jobs/{jobId}` |
| Export CSV | §12 | `GET /reports/export?jobId=` or `?applicationId=` (download file) |

**How to use:** Export returns CSV; trigger browser download (e.g. blob + link).

---

### 11. Bulk import, audit, admin

| Feature | Workflow | Main APIs |
|--------|----------|-----------|
| Bulk import list | §13 | `GET /bulk-import/batches?companyId=` |
| Bulk import detail | §13 | `GET /bulk-import/batches/{id}` |
| Audit logs | §14 | `GET /audit-logs?companyId=&page=&size=` |
| Platform admin dashboard | §14 | `GET /dashboards/platform-admin` (platform_admin only) |
| Notifications test | API-REFERENCE | `POST /notifications/test` |
| Webhooks config | API-REFERENCE | `GET /webhooks/config?companyId=`, `POST /webhooks/config` |

---

## Error handling (all features)

- **4xx/5xx:** JSON body with `status`, `code`, `message`, `details`. Show `message` (and `details` for validation).
- **429:** “Too many requests. Try again later.” Disable action for a short time.
- **401:** Refresh token or redirect to login.
- **403:** “You don’t have access.” Show message and optional link.

---

## Quick reference

| Doc | Use for |
|-----|--------|
| [README.md](README.md) | Overview, base URL, headers, auth, errors |
| [WORKFLOWS.md](WORKFLOWS.md) | Full E2E flows and API order |
| [API-REFERENCE.md](API-REFERENCE.md) | Every endpoint: method, path, body, response |
| **FEATURE-START.md** (this file) | Feature list and where to find workflows/APIs |

Start with **Auth & session**, then **Company & dashboard**, then **Jobs** and **Apply**; add **Interview** and **Proctoring** when you implement the candidate interview flow.
