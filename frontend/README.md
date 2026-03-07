# Future Scope – Frontend

Next.js 16 app for the **Recruitment & AI Interview Platform**. Talks to the [Future Scope Backend](../backend/).

## Docs (this repo)

| Document | Description |
|----------|-------------|
| **[README.md](README.md)** (this file) | Overview and setup |
| **[ALL-FEATURES.md](ALL-FEATURES.md)** | Complete backend feature checklist |
| **[API-REFERENCE.md](API-REFERENCE.md)** | REST endpoints, request/response shapes |
| **[WORKFLOWS.md](WORKFLOWS.md)** | User flows and which APIs to call |
| **[FEATURE-START.md](FEATURE-START.md)** | What to build first and which APIs to use |

## Setup

1. **Backend** must be running at `http://localhost:8080` (see [backend/README.md](../backend/README.md)).
2. **Env:** Copy `.env.local.example` to `.env.local` and set `NEXT_PUBLIC_API_URL` if needed.
3. **Install and run:**
   ```bash
   npm install
   npm run dev
   ```
4. Open [http://localhost:3000](http://localhost:3000).

## Stack

- Next.js 16 (App Router), TypeScript, Tailwind CSS, shadcn/ui.
- Auth: Bearer token; refresh on 401 via `POST /auth/refresh`.

## Testing

- **Unit:** `npm test` (Jest).
- **E2E:** `npm run test:e2e` (Playwright). For recruiter flows (signup → dashboard → jobs, reports, etc.), the **backend must be running** at `http://localhost:8080`; otherwise those tests may time out or fail.
