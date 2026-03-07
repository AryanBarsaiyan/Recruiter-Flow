---
name: nextjs-app-development-plan
overview: Phased plan to build the Future Scope frontend as a Next.js 16 app (App Router, TypeScript, Tailwind), feature-by-feature with unit, component, and E2E testing. After every feature (and phase), end-to-end tests must be written and passing before moving on. Consumes the existing Spring Boot backend.
todos:
  - id: foundation
    content: Scaffold Next.js 16 app, API client with auth/refresh, AuthContext, layouts, Jest + Playwright setup.
    status: pending
  - id: phase1-auth
    content: Auth UI – login, signup (super-admin), logout, refresh; recruiter companyId handling.
    status: pending
  - id: phase2-company
    content: Company profile, members, branding, recruiter dashboard, pipelines.
    status: pending
  - id: phase3-jobs
    content: Jobs CRUD, list (paginated), public job board.
    status: pending
  - id: phase4-apply
    content: Apply to job, application detail, stage progress.
    status: pending
  - id: phase5-candidate
    content: Candidate /me – profile, applications, saved jobs (add/remove).
    status: pending
  - id: phase6-scheduling
    content: Invitation by token, book slot.
    status: pending
  - id: phase7-interview
    content: Interview start, submit code, follow-up answer; proctoring session, events, end.
    status: pending
  - id: phase8-reports
    content: Reports, CSV export, invite/accept-invite, password reset, verify email, bulk import, audit, platform admin, webhooks.
    status: pending
isProject: false
---

# Next.js 16 App – Full Development Plan

## Stack: Next.js 16

- **Framework:** Next.js 16 (App Router). Use `create-next-app` with Next.js 16 when initializing (e.g. `npx create-next-app@16` or latest 16.x).
- **Runtime:** Node.js 20.9+ and TypeScript 5.1+ (per Next.js 16 requirements).
- **Bundler:** Turbopack is the default in Next.js 16; use it for dev and build unless you need to opt out.
- **UI:** TypeScript, Tailwind CSS, and a component library (e.g. shadcn/ui) in [frontend/](frontend/).
- **Backend:** Existing Spring Boot API at `http://localhost:8080`; configure via `NEXT_PUBLIC_API_URL`.

Next.js 16–specific notes:

- **Caching:** Request-time execution by default; use `"use cache"` where you want explicit caching (e.g. static job board snapshot).
- **Routing:** App Router only; layout deduplication and React 19 features apply.
- **Proxy / middleware:** Next.js 16 introduces Proxy.ts for request handling; use it or existing middleware for auth redirects as needed.
- **Turbopack:** Faster dev and builds; no extra config required.

---

## Assumptions and scope

- **Location:** Next.js app lives in [frontend/](frontend/). Existing `.md` docs remain; add `package.json`, `app/`, `components/`, `lib/`, etc. inside `frontend/`.
- **Auth:** Backend returns `accessToken` + `refreshToken` in JSON. Store in memory + localStorage; send `Authorization: Bearer <accessToken>`; on 401 call `POST /auth/refresh` and retry.
- **Recruiter companyId:** Backend does not return companyId in JWT or signup. Use onboarding step or future backend endpoint to set/store default companyId.

---

## Phases (summary)

| Phase | Focus | Key deliverables | E2E gate |
|-------|--------|-------------------|----------|
| **Foundation** | Scaffold, API client, auth context, layouts, testing | Next 16 app runs; API + auth + Jest + Playwright | E2E: app + login page load |
| **1 – Auth** | Login, signup, logout, refresh | Public login/signup; protected layouts | E2E: login, logout, signup, invalid login |
| **2 – Company** | Company, members, branding, dashboard, pipelines | Recruiter dashboard and company page | E2E: dashboard + company page |
| **3 – Jobs** | CRUD, list, public board | Jobs list/detail/edit; public /jobs | E2E: create/list/edit/delete + public board |
| **4 – Apply** | Apply form, application detail, stage | Apply page; application + stage views | E2E: apply + view application/stage |
| **5 – Candidate** | /me profile, applications, saved jobs | Candidate profile and saved jobs | E2E: /me, saved jobs add/remove |
| **6 – Scheduling** | Invitation by token, book slot | Invite page and slot booking | E2E: invite URL + book slot |
| **7 – Interview** | Start, submit code, follow-up; proctoring | Interview room and proctoring | E2E: start interview, submit code, follow-up |
| **8 – Reports** | Reports, export, invite/accept, reset, audit, admin, webhooks | Report views, CSV export, remaining features | E2E: report, CSV export, invite/accept, password reset |

Each phase: implement feature → add component/unit tests → add E2E for critical paths.

---

## Foundation

1. Initialize Next.js **16** in `frontend/`: `npx create-next-app@16 . --typescript --tailwind --eslint --app`.
2. Add `NEXT_PUBLIC_API_URL=http://localhost:8080` in `.env.local`.
3. **lib/api.ts:** fetch wrapper, Bearer token, 401 → refresh then retry, `ApiError` (status, code, message, details), 429 handling.
4. **lib/auth.ts:** token storage (e.g. in-memory accessToken + localStorage refreshToken).
5. **contexts/AuthContext.tsx:** user, companyId (recruiter), login, logout, refresh; provide token to API client.
6. **Layouts:** root (AuthProvider); (public) for login, signup, jobs; (recruiter) and (candidate) protected, redirect to login if no token.
7. **Testing:** Jest + RTL for unit/component; Playwright for E2E. Optional MSW for API mocking.

**E2E (required after Foundation):** Home/public page loads; login page loads and form is submittable (can use mock or real backend).

---

## Phase 1: Auth and session

- **Login** `app/(public)/login/page.tsx`: email, password → `POST /auth/login` → set tokens, redirect by userType.
- **Signup** `app/(public)/signup/page.tsx`: email, password, fullName, companyName → `POST /auth/signup-super-admin` → set tokens, redirect to onboarding/dashboard.
- **Logout:** button → `POST /auth/logout` + clearTokens, redirect to /login.
- **Recruiter companyId:** onboarding step or stored companyId (e.g. from future API); persist in context/localStorage.

**Unit/component tests:** Login/signup form submission and validation errors.

**E2E (required after this phase):** (1) Login with valid credentials → redirect to dashboard or /me. (2) Logout → redirect to login. (3) Signup (super-admin) with new user → redirect to onboarding/dashboard. (4) Login with invalid credentials → error shown. Do not proceed to Phase 2 until these E2E tests pass.

---

## Phase 2: Company and recruiter dashboard

- **Dashboard** `app/(recruiter)/dashboard/page.tsx`: `GET /dashboards/recruiter?companyId=` → totalJobs, totalApplications.
- **Company** `app/(recruiter)/company/page.tsx`: `GET /companies/{id}`, `GET /companies/{id}/members`; optional `PATCH /companies/{id}/branding`.
- **Pipelines:** `GET /companies/{companyId}/pipelines`, `GET /pipelines/{pipelineId}/stages` (page or section).

**Unit/component tests:** Dashboard and company page with mocked API.

**E2E (required after this phase):** (1) Logged-in recruiter opens dashboard → sees totalJobs and totalApplications. (2) Opens company page → sees company name and members list. (3) Optionally: update branding and verify. Do not proceed to Phase 3 until these E2E tests pass.

---

## Phase 3: Jobs

- **List** `app/(recruiter)/jobs/page.tsx`: `GET /jobs?companyId=&page=&size=` with pagination.
- **Create** `app/(recruiter)/jobs/new/page.tsx`: form → `POST /jobs`.
- **Detail** `app/(recruiter)/jobs/[id]/page.tsx`: `GET /jobs/{id}`; Edit and Delete.
- **Edit** `app/(recruiter)/jobs/[id]/edit/page.tsx`: `PUT /jobs/{id}`.
- **Public board** `app/(public)/jobs/page.tsx`: `GET /jobs/public`; link to apply.

**Unit/component tests:** Jobs list and create form with mocked API.

**E2E (required after this phase):** (1) Recruiter creates a job → appears in list. (2) Recruiter opens job detail → edits job → sees updated data. (3) Recruiter deletes job → removed from list. (4) Anonymous user opens public /jobs → sees job board and can open apply link. Do not proceed to Phase 4 until these E2E tests pass.

---

## Phase 4: Apply and applications

- **Apply** `app/(public)/jobs/[id]/apply/page.tsx`: form (email, fullName, phone, resume*, answers) → `POST /jobs/{jobId}/apply`.
- **Application detail** (recruiter/candidate): `GET /applications/{id}`, `GET /applications/{id}/stage`.

**Unit/component tests:** Apply form validation and success state; application detail and stage display with mocked API.

**E2E (required after this phase):** (1) User submits application on public apply page → success message. (2) Recruiter opens application detail → sees application and stage progress. (3) Candidate (if applicable) opens own application → sees status. Do not proceed to Phase 5 until these E2E tests pass.

---

## Phase 5: Candidate /me

- **Profile** `app/(candidate)/me/page.tsx`: `GET /me`.
- **My applications** `app/(candidate)/me/applications/page.tsx`: `GET /me/applications`.
- **Saved jobs** `app/(candidate)/me/saved-jobs/page.tsx`: `GET /me/saved-jobs`, `DELETE /me/saved-jobs/{id}`.
- **Save job:** `POST /me/saved-jobs` from job page when candidate is logged in.

**Unit/component tests:** Profile and saved jobs list with mocked API; save/unsave buttons.

**E2E (required after this phase):** (1) Candidate logs in → opens /me → sees profile and applications. (2) Candidate opens saved jobs → adds a job from board → sees it in saved list. (3) Candidate removes saved job → it disappears from list. Do not proceed to Phase 6 until these E2E tests pass.

---

## Phase 6: Scheduling

- **Invitation** `app/(public)/invite/[token]/page.tsx`: `GET /interview-invitations/{token}`; form scheduledStartAt, scheduledEndAt → `POST /interview-invitations/{token}/slots`.

**Unit/component tests:** Invitation page and book-slot form with mocked API.

**E2E (required after this phase):** (1) User opens invite URL with valid token → sees invitation info and slot form. (2) User submits slot → sees confirmation and “Start interview” (or next step). (3) Invalid token → error or 400 handled. Do not proceed to Phase 7 until these E2E tests pass.

---

## Phase 7: Interview and proctoring

- **Start interview:** `POST /interviews/start` with invitationToken → interviewId, firstQuestionId, etc.; redirect to interview room.
- **Interview room:** submit code `POST .../submit-code`, answer follow-up `POST .../followups/.../answer`.
- **Proctoring:** `POST /proctoring/sessions`; `POST .../events`; `POST .../end`.

**Unit/component tests:** Interview start form; code editor and submit/follow-up buttons with mocked API.

**E2E (required after this phase):** (1) User starts interview with valid invitation token → gets interviewId and first question; lands on interview room. (2) User submits code → receives response (e.g. status/follow-up). (3) User submits follow-up answer → flow continues or completes. (4) Proctoring: start session and end session (events can be smoke-tested or mocked). Do not proceed to Phase 8 until these E2E tests pass.

---

## Phase 8: Reports and remaining features

- Reports: `GET /reports/applications/{id}`, `GET /reports/jobs/{id}`, `GET /reports/export?jobId=` (CSV download).
- Invite/accept-invite, request-password-reset, reset-password, verify-email.
- Bulk import list/detail, audit logs, platform admin dashboard, notifications test, webhooks config.

Tests: Report and export; E2E for invite/accept and password reset if desired.

---

## Testing strategy

- **Unit:** API client (auth header, refresh, ApiError), auth context (login/logout).
- **Component:** Forms and lists with mocked API (MSW or jest.mock).
- **E2E (Playwright):** Required after every feature and every phase. For each phase, add Playwright specs that cover the flows listed in that phase’s “E2E (required after this phase)” section. Run the full E2E suite (or the phase’s E2E) before marking the phase done and moving to the next. No phase is complete without passing E2E tests for that phase.

---

## File structure (suggested)

```
frontend/
  app/
    (public)/       # login, signup, jobs (board), invite, interview start
    (recruiter)/    # dashboard, company, jobs, applications, reports, audit, bulk import
    (candidate)/    # me, me/applications, me/saved-jobs
    layout.tsx
  components/
  contexts/         # AuthContext
  lib/              # api.ts, auth.ts
  hooks/
  e2e/              # Playwright
  __tests__/        # Jest
  *.md              # existing docs
```

---

## Dependencies

- Next.js 16, React 19, TypeScript 5.1+, Tailwind, shadcn/ui (or equivalent).
- react-hook-form, zod.
- jest, @testing-library/react, @playwright/test; optional msw, jose (JWT decode).

This plan uses **Next.js 16** throughout; adjust for 16.x minor versions as needed.
