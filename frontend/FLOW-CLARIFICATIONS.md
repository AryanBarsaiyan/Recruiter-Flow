# Flow clarifications – backend vs frontend

This doc lists **questions for you** so we can align the UI with the backend and user expectations. If the backend returns more data or you want a different flow, we can adjust.

---

## 1. Recruiter: list applications per job

**Current:** There is no `GET /jobs/{jobId}/applications` (or similar) in the API. Recruiters can open an application only if they know its ID (e.g. from report or email).

**Question:** Can the backend add an endpoint to list applications for a job (e.g. `GET /jobs/{jobId}/applications` or `GET /applications?jobId=`) so recruiters can see “Applications” on the job detail page and open each one?

---

## 2. Candidate: “My applications” – job title

**Current:** `GET /me/applications` returns `ApplicationResponse[]` with `id`, `jobId`, `candidateId`, `status`, `appliedAt` but **no job title**. The UI shows “Application #abc… · Job ID: xyz…”.

**Question:** Can the backend add `jobTitle` (and optionally `companyName`) to `ApplicationResponse` so we can show “Applied to Senior Engineer at Acme” without an extra request per application?

---

## 3. Apply to job: resume and custom form

**Aligned.** Frontend uses `POST /jobs/{jobId}/upload-resume` for file upload, then submit with `resumeStoragePath`, `resumeOriginalFilename`. Apply page loads job via `GET /jobs/public/{id}` (includes `customFormSchemaJson`), renders dynamic fields, and sends values in `answers`.

---

## 4. Invite: datetime format for book slot

**Aligned.** Invite page converts local datetime to UTC ISO via `toUtcIsoString()` and sends `scheduledStartAt` / `scheduledEndAt` in ISO 8601.

---

## 5. Auth response: fullName for recruiter

**Current:** Login/signup/refresh return `user`: `{ id, email, userType, defaultCompanyId }`. There is no `fullName` for recruiters (GET /me is only for candidates).

**Question:** Do you want recruiters to see “Welcome, {name}” in the header? If yes, can the backend add `fullName` (or similar) to the auth `user` object for recruiters (e.g. from company member or user profile)?

---

## Summary of what was aligned already

- **Stage progress:** Backend returns an **array** of stage items (not `{ stages: [] }`). Frontend now uses that array and shows `stageName`, `status`.
- **Invitation:** Frontend now shows `jobTitle`, `interviewType`, `expiresAt` from `GET /interview-invitations/{token}` and, after booking a slot, a “Start interview” link to `/interview/start?token=...`.
- **Interview start:** The start page reads `?token=` from the URL and pre-fills the invitation token.
- **Login redirect:** Login supports `?redirect=/interview/xxx` (and other same-origin paths) so candidates can sign in and return to the interview room.
- **Candidate profile:** GET /me returns `fullName`, `phone`, `college`, `graduationYear`; the profile page shows all of them.
- **Saved jobs:** Backend returns `jobTitle` and `savedAt`; the saved-jobs list uses `jobTitle` and shows “Saved {date}”.

If you confirm or change any of the above, we can update the frontend (or the plan) accordingly.
