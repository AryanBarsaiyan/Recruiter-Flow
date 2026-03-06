# Future Scope Backend

Spring Boot 4 backend for the **Recruitment & AI Interview Platform** — auth, companies, jobs, applications, pipelines, scheduling, interviews, proctoring, reporting, and rate limiting.

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Java 21** | [Eclipse Temurin](https://adoptium.net/) or [Microsoft OpenJDK 21](https://learn.microsoft.com/en-us/java/openjdk/download) |
| **PostgreSQL** | For local run; create DB and user (see below) |
| **Docker** | Required only for **integration tests** (Testcontainers) |
| **Maven** | Optional — project includes Maven Wrapper (`mvnw` / `mvnw.cmd`) |

---

## Quick start

### 1. Clone and go to backend

```bash
cd backend
```

### 2. Set up PostgreSQL

Create a database and user (example):

```sql
CREATE USER future_scope WITH PASSWORD 'future_scope';
CREATE DATABASE future_scope_dev OWNER future_scope;
```

For tests, optional:

```sql
CREATE USER future_scope_test WITH PASSWORD 'future_scope_test';
CREATE DATABASE future_scope_test OWNER future_scope_test;
```

### 3. Configure environment (optional)

Copy the sample env file and edit:

```bash
# Windows (PowerShell)
copy env.sample .env
# Edit .env with your DB URL, user, password, and JWT secret

# Linux / macOS
cp env.sample .env
# Edit .env
```

Then load env before running (see [Using .env](#using-env) below), or override in `application.yml` / `application-local.yml`.

### 4. Run the application

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Windows (cmd):**

```cmd
mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

With custom DB (no .env):

```bash
.\mvnw.cmd spring-boot:run -Dspring.datasource.url=jdbc:postgresql://localhost:5432/future_scope_dev -Dspring.datasource.username=future_scope -Dspring.datasource.password=future_scope
```

Server starts at **http://localhost:8080** (or `SERVER_PORT` if set).

---

## How to run tests

**Docker must be running** (integration tests use Testcontainers + PostgreSQL).

**Windows:**

```powershell
.\mvnw.cmd test
```

**Linux / macOS:**

```bash
./mvnw test
```

Quiet mode (less output):

```bash
.\mvnw.cmd test -q
```

Run a single test class:

```bash
.\mvnw.cmd test -Dtest=AuthControllerIntegrationTest
```

If you see `Could not find a valid Docker environment`, start Docker Desktop (or the daemon) and rerun.

---

## Using .env

Spring Boot does not load `.env` by default. Use one of these approaches:

**Option A – Export then run (Linux/macOS):**

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

**Option B – PowerShell (Windows):**

```powershell
Get-Content .env | ForEach-Object {
  if ($_ -match '^\s*([^#=]+)=(.*)$') {
    [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
  }
}
.\mvnw.cmd spring-boot:run
```

**Option C – Override in config**

Rename or copy `env.sample` to `.env`, then put the same values into `src/main/resources/application-local.yml` (and add `application-local.yml` to `.gitignore` if it contains secrets). Spring Boot loads `application-local.yml` automatically when present.

**Option D – System properties**

Pass variables when running:

```bash
.\mvnw.cmd spring-boot:run -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mydb -Dspring.datasource.password=secret
```

---

## Configuration reference

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/future_scope_dev` | PostgreSQL JDBC URL |
| `spring.datasource.username` | `future_scope` | DB user |
| `spring.datasource.password` | `future_scope` | DB password |
| `security.jwt.secret` | (see `application.yml`) | Base64 JWT signing secret; **must change in production** |
| `security.jwt.access-token-minutes` | `60` | Access token validity (minutes) |
| `app.rate-limit.enabled` | `true` | Enable rate limiting |
| `app.rate-limit.auth-requests-per-minute` | `15` | Auth endpoints (per IP) |
| `server.port` | `8080` | HTTP port |

Env vars use relaxed binding, e.g. `SPRING_DATASOURCE_URL`, `SECURITY_JWT_SECRET`, `SERVER_PORT`.

---

## API documentation

When the app is running:

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  

---

## Health and actuator

- **Health:** http://localhost:8080/actuator/health  
- **Info:** http://localhost:8080/actuator/info  

---

## Project layout

```
backend/
├── src/main/java/com/futurescope/platform/
│   ├── auth/          # Auth, companies, RBAC
│   ├── job/            # Jobs, pipelines
│   ├── application/    # Applications, apply
│   ├── candidate/      # /me, saved jobs
│   ├── bulkimport/     # Bulk import batches
│   ├── schedule/       # Interview invitations, slots
│   ├── interview/      # Interview start, submit-code, followups
│   ├── proctoring/     # Proctoring sessions, events
│   ├── reporting/      # Reports, dashboards, export
│   ├── notification/   # Notifications, webhooks
│   ├── audit/          # Audit logs
│   ├── security/       # JWT, rate limiting
│   └── common/         # Exception handling, shared
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/   # Flyway scripts
├── src/test/           # Unit and integration tests
├── env.sample          # Sample env vars (copy to .env)
├── mvnw / mvnw.cmd     # Maven Wrapper
└── pom.xml
```

---

## License

Proprietary / as per project policy.
