# Future Scope – Frontend Documentation

This folder contains the **frontend guide** for the **Recruitment & AI Interview Platform**. Use it to build the web app that talks to the [Future Scope Backend](../backend/).

---

## Contents

| Document | Description |
|----------|-------------|
| **[README.md](README.md)** (this file) | Overview, setup, and how to use this doc |
| **[ALL-FEATURES.md](ALL-FEATURES.md)** | **Complete checklist** – every backend feature (51 items) with endpoint and doc section |
| **[API-REFERENCE.md](API-REFERENCE.md)** | All REST endpoints, request/response shapes, and errors |
| **[WORKFLOWS.md](WORKFLOWS.md)** | End-to-end user flows and which APIs to call in order |
| **[FEATURE-START.md](FEATURE-START.md)** | Feature-to-start guide: what to build first and which APIs to use |

---

## 1. Where to start

1. **Backend must be running**  
   See [backend/README.md](../backend/README.md). Default base URL: `http://localhost:8080`.

2. **Read the workflows**  
   Open [WORKFLOWS.md](WORKFLOWS.md) for recruiter and candidate journeys (auth → company → jobs → applications → interviews → reports).

3. **Use the API reference**  
   Open [API-REFERENCE.md](API-REFERENCE.md) for every endpoint: method, path, auth, body, and response.

4. **Implement the UI**  
   Use your stack (React, Vue, Angular, etc.) and call the APIs as described. Prefer **HTTPS** and **Bearer token** for authenticated requests.

---

## 2. Base URL and headers

- **Base URL:** `http://localhost:8080` (or your deployed backend).
- **JSON:** Send `Content-Type: application/json` for request bodies; responses are JSON unless noted (e.g. CSV export).
- **Auth:** After login/signup/accept-invite, send the access token:
  ```http
  Authorization: Bearer <accessToken>
  ```
- **Refresh:** When the access token expires (e.g. 401/403), call `POST /auth/refresh` with `refreshToken` and store the new `accessToken`/`refreshToken`.

---

## 3. Error handling

- **4xx/5xx** responses return a JSON body like:
  ```json
  {
    "timestamp": "2025-03-07T00:00:00Z",
    "status": 400,
    "code": "BAD_REQUEST",
    "message": "Human-readable message",
    "details": ["field: validation message"]
  }
  ```
- **429 Too Many Requests:** Rate limit exceeded; show “Try again later” and optionally retry after a delay.
- **401 Unauthorized:** Invalid or expired token; redirect to login or refresh.
- **403 Forbidden:** Not allowed (e.g. wrong company or role); show an error message.
- Use `code` and `message` (and `details` for validation) to drive UI messages.

---

## 4. Auth and roles

- **Recruiter:** `userType === "recruiter"`. Can access company, jobs, applications, pipelines, dashboards, reports, bulk import, audit (within their company). Needs at least one **company membership** (SuperAdmin, Admin, or ReadOnly).
- **Candidate:** `userType === "candidate"`. Can access `/me`, `/me/applications`, `/me/saved-jobs`, apply to jobs, start interviews, submit code, answer follow-ups, proctoring events.
- **Platform admin:** `userType === "platform_admin"`. Can call `/dashboards/platform-admin` and audit logs (platform-wide).
- After **login** or **accept-invite**, the backend returns `accessToken` and `refreshToken`; store them securely and send `Authorization: Bearer <accessToken>` on every authenticated request.

---

## 5. OpenAPI / Swagger

When the backend is running:

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

You can generate a client or use these for request/response schemas.

---

## 6. Feature checklist (for implementation)

**Definitive list:** See **[ALL-FEATURES.md](ALL-FEATURES.md)** for all 51 items (every endpoint + doc section). Summary below; details in [WORKFLOWS.md](WORKFLOWS.md) and [API-REFERENCE.md](API-REFERENCE.md).

- [ ] **Auth (9):** signup super-admin, login, refresh, logout, invite, accept-invite, verify-email, request-password-reset, reset-password
- [ ] **Companies (3):** get company, list members, update branding
- [ ] **Jobs (6):** create, get, update, delete, list by company (paginated), public job board
- [ ] **Apply (1):** apply to job (public)
- [ ] **Applications (2):** get application, get stage progress
- [ ] **Candidate /me (5):** get profile, list applications, list saved jobs, save job, unsave job
- [ ] **Bulk import (2):** list batches, get batch by id
- [ ] **Pipelines (2):** list by company, get stages
- [ ] **Scheduling (2):** get invitation by token, book slot
- [ ] **Interviews (4):** start, get, submit code, answer follow-up
- [ ] **Proctoring (3):** start session, post events, end session
- [ ] **Reports (3):** by application, by job, export CSV
- [ ] **Dashboards (2):** recruiter, platform-admin
- [ ] **Notifications & webhooks (3):** test notification, get webhook config, update webhook config
- [ ] **Audit (1):** list audit logs (company-scoped)
- [ ] **Cross-cutting:** handle 429 (rate limit), error JSON, pagination

---

## 7. License

Proprietary / as per project policy.
