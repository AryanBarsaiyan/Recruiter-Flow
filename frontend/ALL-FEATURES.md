# All Features – Complete Checklist

This is the **definitive list** of every backend feature the frontend can use. Each row maps to the backend API; see [API-REFERENCE.md](API-REFERENCE.md) for request/response details and [WORKFLOWS.md](WORKFLOWS.md) for usage order.

---

## Auth (9)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 1 | Super-admin signup | `POST /auth/signup-super-admin` | API §1, Workflows §1 |
| 2 | Login | `POST /auth/login` | API §1, Workflows §2 |
| 3 | Refresh token | `POST /auth/refresh` | API §1, Workflows §2 |
| 4 | Logout | `POST /auth/logout` | API §1, Workflows §2 |
| 5 | Invite member | `POST /auth/invite` | API §1, Workflows §3 |
| 6 | Accept invite | `POST /auth/accept-invite` | API §1, Workflows §3 |
| 7 | Verify email | `POST /auth/verify-email` | API §1, Workflows §15 |
| 8 | Request password reset | `POST /auth/request-password-reset` | API §1, Workflows §15 |
| 9 | Reset password | `POST /auth/reset-password` | API §1, Workflows §15 |

---

## Companies (3)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 10 | Get company | `GET /companies/{id}` | API §2, Workflows §4 |
| 11 | List company members | `GET /companies/{id}/members` | API §2, Workflows §4 |
| 12 | Update company branding | `PATCH /companies/{id}/branding` | API §2, Workflows §4 |

---

## Jobs (6)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 13 | Create job | `POST /jobs` | API §3, Workflows §5 |
| 14 | Get job | `GET /jobs/{id}` | API §3, Workflows §5 |
| 15 | Update job | `PUT /jobs/{id}` | API §3, Workflows §5 |
| 16 | Delete job | `DELETE /jobs/{id}` | API §3, Workflows §5 |
| 17 | List jobs (by company, paginated) | `GET /jobs?companyId=&page=&size=` | API §3, Workflows §5 |
| 18 | Public job board | `GET /jobs/public` | API §3, Workflows §6 |

---

## Apply (1)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 19 | Apply to job | `POST /jobs/{jobId}/apply` | API §4, Workflows §6 |

---

## Applications (2)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 20 | Get application | `GET /applications/{id}` | API §5, Workflows §8 |
| 21 | Get application stage progress | `GET /applications/{id}/stage` | API §5, Workflows §8 |

---

## Candidate / Me (5)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 22 | Get my profile | `GET /me` | API §6, Workflows §7 |
| 23 | List my applications | `GET /me/applications` | API §6, Workflows §7 |
| 24 | List saved jobs | `GET /me/saved-jobs` | API §6, Workflows §7 |
| 25 | Save a job | `POST /me/saved-jobs` | API §6, Workflows §7 |
| 26 | Unsave a job | `DELETE /me/saved-jobs/{id}` | API §6, Workflows §7 |

---

## Bulk import (2)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 27 | List bulk import batches | `GET /bulk-import/batches?companyId=` | API §7, Workflows §13 |
| 28 | Get bulk import batch | `GET /bulk-import/batches/{id}` | API §7, Workflows §13 |

---

## Pipelines (2)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 29 | List pipelines (by company) | `GET /companies/{companyId}/pipelines` | API §8, Workflows §4 |
| 30 | Get pipeline stages | `GET /pipelines/{pipelineId}/stages` | API §8, Workflows §4 |

---

## Scheduling (2)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 31 | Get invitation by token | `GET /interview-invitations/{token}` | API §9, Workflows §9 |
| 32 | Book interview slot | `POST /interview-invitations/{token}/slots` | API §9, Workflows §9 |

---

## Interviews (4)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 33 | Start interview | `POST /interviews/start` | API §10, Workflows §10 |
| 34 | Get interview | `GET /interviews/{id}` | API §10, Workflows §10 |
| 35 | Submit code | `POST /interviews/.../questions/.../submit-code` | API §10, Workflows §10 |
| 36 | Answer follow-up | `POST /interviews/.../followups/.../answer` | API §10, Workflows §10 |

---

## Proctoring (3)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 37 | Start proctoring session | `POST /proctoring/sessions` | API §11, Workflows §11 |
| 38 | Send proctoring event | `POST /proctoring/sessions/{sessionId}/events` | API §11, Workflows §11 |
| 39 | End proctoring session | `POST /proctoring/sessions/{sessionId}/end` | API §11, Workflows §11 |

---

## Reports (3)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 40 | Get report by application | `GET /reports/applications/{applicationId}` | API §12, Workflows §12 |
| 41 | Get summary by job | `GET /reports/jobs/{jobId}` | API §12, Workflows §12 |
| 42 | Export CSV | `GET /reports/export?jobId= or ?applicationId=` | API §12, Workflows §12 |

---

## Dashboards (2)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 43 | Recruiter dashboard | `GET /dashboards/recruiter?companyId=` | API §13, Workflows §13 |
| 44 | Platform admin dashboard | `GET /dashboards/platform-admin` | API §13, Workflows §14 |

---

## Notifications & webhooks (3)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 45 | Test notification | `POST /notifications/test` | API §14 |
| 46 | Get webhook config | `GET /webhooks/config?companyId=` | API §14 |
| 47 | Update webhook config | `POST /webhooks/config` | API §14 |

---

## Audit (1)

| # | Feature | Endpoint | Doc section |
|---|---------|----------|-------------|
| 48 | List audit logs | `GET /audit-logs?companyId=&page=&size=` | API §15, Workflows §14 |

---

## Cross-cutting

| # | Feature | Notes | Doc section |
|---|---------|--------|-------------|
| 49 | Rate limiting (429) | Handle on auth, interview, proctoring; show “Try again later” | README §3, Workflows §16 |
| 50 | Error responses | All 4xx/5xx return JSON: status, code, message, details | API-REFERENCE (top), README §3 |
| 51 | Pagination | Jobs list, audit logs: page, size; response has content, totalElements, totalPages | API §3, §15, §16 |

---

## Summary

| Area | Count |
|------|-------|
| Auth | 9 |
| Companies | 3 |
| Jobs | 6 |
| Apply | 1 |
| Applications | 2 |
| Candidate / Me | 5 |
| Bulk import | 2 |
| Pipelines | 2 |
| Scheduling | 2 |
| Interviews | 4 |
| Proctoring | 3 |
| Reports | 3 |
| Dashboards | 2 |
| Notifications & webhooks | 3 |
| Audit | 1 |
| Cross-cutting | 3 |
| **Total** | **51 items** |

All backend features exposed to the frontend are listed above. There is no separate “job config” endpoint; job configuration (resume criteria, form schema, etc.) is returned as part of `GET /jobs/{id}`.
