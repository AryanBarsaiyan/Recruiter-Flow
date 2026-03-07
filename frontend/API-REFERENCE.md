# API Reference – Future Scope Backend

Base URL: `http://localhost:8080` (or your backend URL).  
All authenticated endpoints require: `Authorization: Bearer <accessToken>`.

**Public** = no auth. **Auth** = Bearer token required.

---

## Errors (all 4xx/5xx)

```json
{
  "timestamp": "2025-03-07T00:00:00Z",
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "Human-readable message",
  "details": ["fieldName: validation message"]
}
```

- **429:** Rate limit; body may include `"error": "Too Many Requests"`, `"message": "Rate limit exceeded. Please try again later."`

---

## 1. Auth

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/signup-super-admin` | Public | Create first company + SuperAdmin user |
| POST | `/auth/login` | Public | Login; returns tokens |
| POST | `/auth/refresh` | Public | New access + refresh token |
| POST | `/auth/logout` | Auth | Invalidate current session |
| POST | `/auth/invite` | Auth | Invite user to company (SuperAdmin/Admin) |
| POST | `/auth/accept-invite` | Public | Accept invite with token + optional password |
| POST | `/auth/verify-email` | Public | Verify email (token in body) |
| POST | `/auth/request-password-reset` | Public | Request password reset (email in body) |
| POST | `/auth/reset-password` | Public | Reset password (token + newPassword in body) |

### Request / response

**POST /auth/signup-super-admin**  
Request:
```json
{
  "email": "string",
  "password": "string",
  "fullName": "string",
  "companyName": "string"
}
```
Response: `200` → `{ "accessToken", "tokenType": "Bearer", "refreshToken", "user" }`

**POST /auth/login**  
Request: `{ "email", "password" }`  
Response: same as signup (includes `user`).

**POST /auth/refresh**  
Request: `{ "refreshToken": "string" }`  
Response: same as signup (includes `user`).

**Auth response shape (login, signup, refresh, accept-invite):**
```json
{
  "accessToken": "string",
  "tokenType": "Bearer",
  "refreshToken": "string",
  "user": {
    "id": "uuid",
    "email": "string",
    "userType": "recruiter" | "candidate" | "platform_admin",
    "defaultCompanyId": "uuid | null"
  }
}
```
For recruiters, `defaultCompanyId` is the first active company (used for dashboard). For candidates it is null.

**POST /auth/logout**  
No body. Response: `204 No Content`.

**POST /auth/invite**  
Request:
```json
{
  "companyId": "uuid",
  "email": "string",
  "roleName": "string"
}
```
Role names: e.g. `"Admin"`, `"ReadOnly"`.  
Response: `200` → `{ "inviteToken", "expiresAt" }`

**POST /auth/accept-invite**  
Request: `{ "token": "string", "password": "string" }`  
Response: `200` → `{ "accessToken", "tokenType", "refreshToken", "user" }`

**POST /auth/verify-email**  
Request: `{ "token": "string" }`  
Response: `200`

**POST /auth/request-password-reset**  
Request: `{ "email": "string" }`  
Response: `200`

**POST /auth/reset-password**  
Request: `{ "token": "string", "newPassword": "string" }`  
Response: `200`

---

## 2. Companies

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/companies/{id}` | Auth, company member | Get company |
| GET | `/companies/{id}/members` | Auth, company member | List members |
| PATCH | `/companies/{id}/branding` | Auth, company member | Update branding |

**GET /companies/{id}**  
Response: `200` → `{ "id", "name", "slug", "brandingConfigJson" }`

**PATCH /companies/{id}/branding**  
Request: `{ "brandingConfigJson": "string" }`  
Response: `200` → company object.

---

## 3. Jobs

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/jobs` | Auth, recruiter | Create job |
| GET | `/jobs/{id}` | Auth, recruiter/candidate | Get job |
| PUT | `/jobs/{id}` | Auth, recruiter | Update job |
| DELETE | `/jobs/{id}` | Auth, recruiter | Delete job |
| GET | `/jobs?companyId=&page=&size=` | Auth, recruiter | List jobs (paginated) |
| GET | `/jobs/public` | Public | Public job board |

**POST /jobs**  
Request (all optional except `companyId`, `title`):
```json
{
  "companyId": "uuid",
  "title": "string",
  "description": "string",
  "location": "string",
  "employmentType": "string",
  "applicationDeadline": "ISO8601",
  "maxApplications": number,
  "resumeCriteriaJson": "string",
  "customFormSchemaJson": "string",
  "scoringWeightsOverrideJson": "string",
  "pipelineId": "uuid"
}
```
Response: `200` → job object.

**PUT /jobs/{id}**  
Request: same fields as create (partial update).  
Response: `200` → job object.

**GET /jobs?companyId=**  
Returns Spring Page: `{ "content": [JobResponse], "totalElements", "totalPages", "size", "number" }`.

**GET /jobs/public**  
Response: `200` → array of job objects.

---

## 4. Apply (candidate)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/jobs/{jobId}/apply` | Public | Submit application |

**POST /jobs/{jobId}/apply**  
Request:
```json
{
  "email": "string",
  "fullName": "string",
  "phone": "string",
  "resumeStoragePath": "string",
  "resumeOriginalFilename": "string",
  "answers": { "fieldKey": value }
}
```
Response: `200` → application object (e.g. `{ "id", "jobId", "status", "appliedAt" }`).

---

## 5. Applications

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/applications/{id}` | Auth, recruiter or candidate | Get application |
| GET | `/applications/{id}/stage` | Auth | Get stage progress |

**GET /applications/{id}/stage**  
Response: `200` → **array** of `{ "id", "stageId", "stageName", "status", "startedAt", "completedAt", "notes" }` (not wrapped in `stages`).

---

## 6. Candidate / Me

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/me` | Auth, candidate | Get my profile |
| GET | `/me/applications` | Auth, candidate | List my applications |
| GET | `/me/saved-jobs` | Auth, candidate | List saved jobs |
| POST | `/me/saved-jobs` | Auth, candidate | Save a job |
| DELETE | `/me/saved-jobs/{id}` | Auth, candidate | Unsave a job |

**GET /me** (candidate only)  
Response: `200` → `{ "id", "email", "fullName", "phone", "college", "graduationYear" }`.

**GET /me/saved-jobs**  
Response: `200` → array of `{ "id", "jobId", "jobTitle", "savedAt" }`.

**POST /me/saved-jobs**  
Request: `{ "jobId": "uuid" }`  
Response: `200` → saved job object.

---

## 7. Bulk import

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/bulk-import/batches?companyId=` | Auth, company member | List batches |
| GET | `/bulk-import/batches/{id}` | Auth, company member | Get batch + candidateCount |

Response for list: array of `{ "id", "companyId", "status", "sourceFilePath", "createdAt" }`.  
Response for get: same + `candidateCount`.

---

## 8. Pipelines

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/companies/{companyId}/pipelines` | Auth, company member | List pipelines |
| GET | `/pipelines/{pipelineId}/stages` | Auth, company member | List stages |

Stages: `{ "id", "pipelineId", "name", "type", "orderIndex" }`.

---

## 9. Scheduling (invitations)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/interview-invitations/{token}` | Public | Get invitation info by token |
| POST | `/interview-invitations/{token}/slots` | Public | Book a slot |

**GET /interview-invitations/{token}**  
Response: `200` → `{ "invitationId", "applicationId", "jobId", "jobTitle", "interviewType", "expiresAt", "status" }`.

**POST /interview-invitations/{token}/slots**  
Request: `{ "scheduledStartAt": "ISO8601", "scheduledEndAt": "ISO8601" }`  
Response: `200` → `{ "id", "scheduledStartAt", "scheduledEndAt", "bookedByCandidateAt" }`.

---

## 10. Interviews

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/interviews/start` | Public | Start interview (invitation token) |
| GET | `/interviews/{id}` | Auth, candidate or recruiter | Get interview |
| POST | `/interviews/{interviewId}/questions/{interviewQuestionId}/submit-code` | Auth, candidate | Submit code |
| POST | `/interviews/{interviewId}/followups/{followupQuestionId}/answer` | Auth, candidate | Submit follow-up answer |

**POST /interviews/start**  
Request: `{ "invitationToken": "string" }`  
Response: `200` → `{ "interviewId", "firstQuestionId", "firstQuestionTitle", "firstQuestionDescription", "firstQuestionStarterCode" }`.

**POST .../submit-code**  
Request: `{ "language": "string", "code": "string" }`  
Response: `200` → `{ "status", "interviewQuestionId" }`.

**POST .../followups/.../answer**  
Request: `{ "answerText": "string" }`  
Response: `200` → `{ "status", "followupQuestionId" }`.

---

## 11. Proctoring

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/proctoring/sessions` | Auth | Start proctoring session |
| POST | `/proctoring/sessions/{sessionId}/events` | Auth | Send event |
| POST | `/proctoring/sessions/{sessionId}/end` | Auth | End session |

**POST /proctoring/sessions**  
Request: `{ "interviewId": "uuid" }`  
Response: `200` → session object (e.g. `id`, `interviewId`).

**POST .../events**  
Request: `{ "eventType": "string", "detailsJson": "string", "weight": number }`  
Response: `200`.

**POST .../end**  
No body. Response: `200` (may include risk summary).

---

## 12. Reports

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/reports/applications/{applicationId}` | Auth, recruiter | Report for application |
| GET | `/reports/jobs/{jobId}` | Auth, recruiter | Summary for job |
| GET | `/reports/export?jobId=&applicationId=` | Auth, recruiter | CSV export |

**GET /reports/export**  
Use either `jobId` or `applicationId` query param. Response: `200`, `Content-Type: text/csv`, body is CSV. Use as download (e.g. `Content-Disposition: attachment`).

---

## 13. Dashboards

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/dashboards/recruiter?companyId=` | Auth, company member | Recruiter dashboard |
| GET | `/dashboards/platform-admin` | Auth, platform_admin | Platform admin dashboard |

Recruiter response: e.g. `{ "companyId", "totalJobs", "totalApplications" }`.  
Platform admin: e.g. `{ "totalCompanies", "totalUsers" }`.

---

## 14. Notifications & webhooks

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/notifications/test` | Auth | Test notification |
| GET | `/webhooks/config?companyId=` | Auth | Get webhook config |
| POST | `/webhooks/config` | Auth | Update webhook config (stub) |

---

## 15. Audit

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/audit-logs?companyId=&page=&size=` | Auth, company member | List audit logs |

Response: paginated list of audit entries.

---

## 16. Pagination (Spring Page)

Where applicable (e.g. jobs list, audit logs):

- Query: `?page=0&size=20`
- Response: `{ "content": [...], "totalElements", "totalPages", "size", "number", "first", "last" }`

---

## 17. IDs and dates

- All IDs are **UUID** strings.
- Dates/times are **ISO-8601** (e.g. `2025-03-07T12:00:00Z` or with offset). Use these for `applicationDeadline`, `scheduledStartAt`, `scheduledEndAt`, etc.
