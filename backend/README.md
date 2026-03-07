# Future Scope Backend

Spring Boot 4 backend for the **Recruitment & AI Interview Platform** — auth, companies, jobs, applications, pipelines, scheduling, interviews, proctoring, reporting, and rate limiting.

---

## Prerequisites

| Requirement    | Notes                                                                                                                       |
| -------------- | --------------------------------------------------------------------------------------------------------------------------- |
| **Java 21**    | [Eclipse Temurin](https://adoptium.net/) or [Microsoft OpenJDK 21](https://learn.microsoft.com/en-us/java/openjdk/download) |
| **PostgreSQL** | For local run; create DB and user (see below)                                                                               |
| **Docker**     | Required only for **integration tests** (Testcontainers)                                                                    |
| **Maven**      | Optional — project includes Maven Wrapper (`mvnw` / `mvnw.cmd`)                                                             |

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

### E2E test candidate seed

To run frontend e2e tests for the candidate profile page, seed a test candidate on startup:

```bash
# Windows
set APP_E2E_SEED_ENABLED=true
.\mvnw.cmd spring-boot:run

# Linux / macOS
APP_E2E_SEED_ENABLED=true ./mvnw spring-boot:run
```

This creates `test-candidate@example.com` / `password123` if it does not exist.

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

**Recommended: Spring Boot native import**

The project loads `.env` via `spring.config.import` (see `application.properties`). No need to export variables manually — just run from the **backend** directory:

```bash
.\mvnw spring-boot:run
```

Create or copy `.env` from `env.sample` and set at least:

```properties
DB_URL=jdbc:postgresql://localhost:5432/your-db-name
DB_USERNAME=your-db-user
DB_PASSWORD=your-db-password
```

`application.properties` uses:

- `spring.config.import=optional:file:.env[.properties]` — load `.env` from the working directory (optional = no error if missing)
- `spring.datasource.url=${DB_URL:default}`, etc. — use `.env` values with fallback defaults

**Other options (if you prefer not to use .env file):**

**Option A – Export then run (Linux/macOS):**

```bash
set -a
source .env
set +a
./mvnw spring-boot:run
```

**Option B – Override in config**

Put values into `src/main/resources/application-local.yml` (add to `.gitignore` if it contains secrets). Spring Boot loads `application-local.yml` when present.

**Option C – System properties**

```bash
.\mvnw.cmd spring-boot:run -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mydb -Dspring.datasource.password=secret
```

---

## Database migrations (Flyway)

Schema is managed by **Flyway**. On a **new or empty database**, apply migrations before or during the first app start.

**Option 1 – Let the app run migrations (recommended)**  
Start the app with your `.env` loaded (see [Using .env](#using-env)). Spring Boot runs Flyway on startup and creates/updates the schema automatically.

**Option 2 – Run Flyway from Maven first**  
Useful if you want to migrate without starting the app. Set Flyway env vars, then run migrate:

**Windows (PowerShell):**

```powershell
$env:FLYWAY_URL="jdbc:postgresql://localhost:5432/your-db-name"
$env:FLYWAY_USER="your-db-user"
$env:FLYWAY_PASSWORD="your-db-password"
.\mvnw flyway:migrate
```

**Linux / macOS:**

```bash
export FLYWAY_URL="jdbc:postgresql://localhost:5432/your-db-name"
export FLYWAY_USER="your-db-user"
export FLYWAY_PASSWORD="your-db-password"
./mvnw flyway:migrate
```

The Flyway Maven plugin is configured in `pom.xml` with the **PostgreSQL database plugin** (`flyway-database-postgresql`) so `flyway:migrate` can connect to PostgreSQL. After migrations succeed, start the app as usual (with the same DB in `.env` or config).

---

## Configuration reference

All of these can be set in `.env` (loaded via `spring.config.import`). Keys in `.env` map as below:

| Property                                                | .env key                                                | Default                                             | Description                                 |
| ------------------------------------------------------- | ------------------------------------------------------- | --------------------------------------------------- | ------------------------------------------- |
| `spring.datasource.url`                                 | `DB_URL`                                                | `jdbc:postgresql://localhost:5432/future_scope_dev` | PostgreSQL JDBC URL                         |
| `spring.datasource.username`                            | `DB_USERNAME`                                           | `future_scope`                                      | DB user                                     |
| `spring.datasource.password`                            | `DB_PASSWORD`                                           | `future_scope`                                      | DB password                                 |
| `security.jwt.secret`                                   | `SECURITY_JWT_SECRET`                                   | (see `application.properties`)                      | Base64 JWT secret; **change in production** |
| `security.jwt.access-token-minutes`                     | `SECURITY_JWT_ACCESS_TOKEN_MINUTES`                     | `60`                                                | Access token validity (minutes)             |
| `app.base-url`                                          | `APP_BASE_URL`                                          | `https://app.example.com`                           | App base URL                                |
| `app.rate-limit.enabled`                                | `APP_RATE_LIMIT_ENABLED`                                | `true`                                              | Enable rate limiting                        |
| `app.rate-limit.auth-requests-per-minute`               | `APP_RATE_LIMIT_AUTH_REQUESTS_PER_MINUTE`               | `15`                                                | Auth endpoints (per IP)                     |
| `app.rate-limit.interview-requests-per-minute`          | `APP_RATE_LIMIT_INTERVIEW_REQUESTS_PER_MINUTE`          | `30`                                                | Interview endpoints                         |
| `app.rate-limit.proctoring-session-requests-per-minute` | `APP_RATE_LIMIT_PROCTORING_SESSION_REQUESTS_PER_MINUTE` | `10`                                                | Proctoring session                          |
| `app.rate-limit.proctoring-events-per-minute`           | `APP_RATE_LIMIT_PROCTORING_EVENTS_PER_MINUTE`           | `120`                                               | Proctoring events                           |
| `server.port`                                           | `SERVER_PORT`                                           | `8080`                                              | HTTP port                                   |

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
