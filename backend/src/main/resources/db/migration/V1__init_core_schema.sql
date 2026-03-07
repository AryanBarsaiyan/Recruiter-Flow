-- Core schema initialization based on db-design.md
-- Auth & RBAC

CREATE TABLE users (
    id                  UUID PRIMARY KEY,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    user_type           VARCHAR(32)  NOT NULL,
    full_name           VARCHAR(255),
    avatar_storage_path VARCHAR(512),
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_login_at       TIMESTAMPTZ
);

CREATE TABLE companies (
    id               UUID PRIMARY KEY,
    name             VARCHAR(255) NOT NULL,
    slug             VARCHAR(255) NOT NULL UNIQUE,
    branding_config  JSONB,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
    id      UUID PRIMARY KEY,
    name    VARCHAR(64) NOT NULL,
    scope   VARCHAR(32) NOT NULL
);

CREATE TABLE company_members (
    id                 UUID PRIMARY KEY,
    company_id         UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    user_id            UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id            UUID NOT NULL REFERENCES roles(id),
    invited_by_user_id UUID REFERENCES users(id),
    status             VARCHAR(32) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (company_id, user_id)
);

CREATE TABLE user_sessions (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at    TIMESTAMPTZ  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions (user_id);

CREATE TABLE email_verifications (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE password_resets (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE mfa_methods (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    method_type VARCHAR(64) NOT NULL,
    secret_data JSONB,
    is_primary  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Candidate data

CREATE TABLE candidates (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name           VARCHAR(255) NOT NULL,
    phone               VARCHAR(50),
    college             VARCHAR(255),
    graduation_year     INTEGER,
    avatar_storage_path VARCHAR(512),
    extra_metadata      JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE resumes (
    id                UUID PRIMARY KEY,
    candidate_id      UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    storage_path      VARCHAR(512) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    parsed_text       TEXT,
    parsed_metadata   JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE saved_jobs (
    id           UUID PRIMARY KEY,
    candidate_id UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    job_id       UUID NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (candidate_id, job_id)
);

CREATE TABLE bulk_import_batches (
    id                 UUID PRIMARY KEY,
    company_id         UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    uploaded_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    source_file_path   VARCHAR(512) NOT NULL,
    status             VARCHAR(32)  NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE bulk_import_candidates (
    id                 UUID PRIMARY KEY,
    batch_id           UUID NOT NULL REFERENCES bulk_import_batches(id) ON DELETE CASCADE,
    candidate_email    VARCHAR(255) NOT NULL,
    candidate_name     VARCHAR(255),
    college            VARCHAR(255),
    linked_candidate_id UUID REFERENCES candidates(id) ON DELETE SET NULL,
    job_id             UUID REFERENCES companies(id),
    status             VARCHAR(32) NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Jobs, applications & pipelines

CREATE TABLE pipelines (
    id             UUID PRIMARY KEY,
    company_id     UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    name           VARCHAR(255) NOT NULL,
    is_default     BOOLEAN      NOT NULL DEFAULT FALSE,
    definition_meta JSONB,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE jobs (
    id                         UUID PRIMARY KEY,
    company_id                 UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    title                      VARCHAR(255) NOT NULL,
    description                TEXT,
    location                   VARCHAR(255),
    employment_type            VARCHAR(64),
    is_published               BOOLEAN      NOT NULL DEFAULT FALSE,
    application_deadline       TIMESTAMPTZ,
    max_applications           INTEGER,
    resume_criteria            JSONB,
    custom_form_schema         JSONB,
    scoring_weights_override   JSONB,
    created_by_user_id         UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
    pipeline_id                UUID REFERENCES pipelines(id) ON DELETE SET NULL,
    created_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                 TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_jobs_company_published ON jobs (company_id, is_published);

CREATE TABLE job_custom_fields (
    id         UUID PRIMARY KEY,
    job_id     UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    field_key  VARCHAR(128) NOT NULL,
    label      VARCHAR(255) NOT NULL,
    field_type VARCHAR(64)  NOT NULL,
    is_required BOOLEAN     NOT NULL DEFAULT FALSE,
    options    JSONB
);

CREATE TABLE job_applications (
    id               UUID PRIMARY KEY,
    job_id           UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    candidate_id     UUID NOT NULL REFERENCES candidates(id) ON DELETE CASCADE,
    resume_id        UUID NOT NULL REFERENCES resumes(id) ON DELETE RESTRICT,
    status           VARCHAR(64) NOT NULL,
    current_stage_id UUID,
    applied_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_status_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source           VARCHAR(64),
    CONSTRAINT uq_job_candidate UNIQUE (job_id, candidate_id)
);

CREATE INDEX idx_job_applications_job_status ON job_applications (job_id, status);
CREATE INDEX idx_job_applications_candidate ON job_applications (candidate_id, job_id);

CREATE TABLE application_answers (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    field_key      VARCHAR(128) NOT NULL,
    value          JSONB,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pipeline_stages (
    id          UUID PRIMARY KEY,
    pipeline_id UUID NOT NULL REFERENCES pipelines(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(64)  NOT NULL,
    order_index INTEGER      NOT NULL,
    config      JSONB
);

CREATE TABLE application_stage_progress (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    stage_id       UUID NOT NULL REFERENCES pipeline_stages(id) ON DELETE CASCADE,
    status         VARCHAR(32) NOT NULL,
    started_at     TIMESTAMPTZ,
    completed_at   TIMESTAMPTZ,
    notes          TEXT
);

-- AI resume screening & scheduling

CREATE TABLE resume_screenings (
    id               UUID PRIMARY KEY,
    application_id   UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    job_id           UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    resume_id        UUID NOT NULL REFERENCES resumes(id) ON DELETE CASCADE,
    match_score      NUMERIC(5,2),
    result           VARCHAR(32) NOT NULL,
    explanation      JSONB,
    criteria_snapshot JSONB,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resume_screenings_application ON resume_screenings (application_id);

CREATE TABLE interview_invitations (
    id             UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    interview_type VARCHAR(32) NOT NULL,
    token          VARCHAR(255) NOT NULL UNIQUE,
    expires_at     TIMESTAMPTZ  NOT NULL,
    status         VARCHAR(32)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE interview_slots (
    id                    UUID PRIMARY KEY,
    invitation_id         UUID NOT NULL REFERENCES interview_invitations(id) ON DELETE CASCADE,
    scheduled_start_at    TIMESTAMPTZ NOT NULL,
    scheduled_end_at      TIMESTAMPTZ NOT NULL,
    booked_by_candidate_at TIMESTAMPTZ,
    cancelled_at          TIMESTAMPTZ,
    cancelled_by          VARCHAR(32),
    reschedule_count      INTEGER     NOT NULL DEFAULT 0,
    no_show_candidate     BOOLEAN     NOT NULL DEFAULT FALSE,
    no_show_recruiter     BOOLEAN     NOT NULL DEFAULT FALSE
);

-- Question bank & interview engine

CREATE TABLE question_bank_questions (
    id            UUID PRIMARY KEY,
    company_id    UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    job_id        UUID REFERENCES jobs(id) ON DELETE SET NULL,
    title         VARCHAR(255) NOT NULL,
    description   TEXT         NOT NULL,
    starter_code  TEXT,
    difficulty    VARCHAR(32),
    topics        JSONB,
    max_score     NUMERIC(5,2),
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE question_test_cases (
    id             UUID PRIMARY KEY,
    question_id    UUID NOT NULL REFERENCES question_bank_questions(id) ON DELETE CASCADE,
    input          TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    visibility     VARCHAR(32) NOT NULL,
    weight         NUMERIC(5,2),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE interviews (
    id            UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    type          VARCHAR(32) NOT NULL,
    status        VARCHAR(32) NOT NULL,
    started_at    TIMESTAMPTZ,
    ended_at      TIMESTAMPTZ,
    total_score   NUMERIC(5,2),
    risk_score    NUMERIC(5,2),
    engine_metadata JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interviews_application ON interviews (application_id);

CREATE TABLE interview_questions (
    id              UUID PRIMARY KEY,
    interview_id    UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    question_id     UUID NOT NULL REFERENCES question_bank_questions(id),
    sequence_number INTEGER     NOT NULL,
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);

CREATE TABLE code_submissions (
    id                  UUID PRIMARY KEY,
    interview_question_id UUID NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    language            VARCHAR(64) NOT NULL,
    code                TEXT        NOT NULL,
    submitted_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    execution_result    JSONB,
    test_cases_passed   INTEGER,
    test_cases_total    INTEGER,
    score_awarded       NUMERIC(5,2)
);

CREATE TABLE followup_questions (
    id                  UUID PRIMARY KEY,
    interview_question_id UUID NOT NULL REFERENCES interview_questions(id) ON DELETE CASCADE,
    prompt              TEXT        NOT NULL,
    factor              VARCHAR(64),
    asked_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE followup_answers (
    id               UUID PRIMARY KEY,
    followup_question_id UUID NOT NULL REFERENCES followup_questions(id) ON DELETE CASCADE,
    answer_text      TEXT        NOT NULL,
    answer_metadata  JSONB,
    score_awarded    NUMERIC(5,2),
    answered_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE manual_interviews (
    id              UUID PRIMARY KEY,
    interview_id    UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    meet_link       VARCHAR(512),
    recording_path  VARCHAR(512),
    reviewer_user_id UUID REFERENCES users(id),
    review_notes    TEXT,
    decision        VARCHAR(32),
    decision_at     TIMESTAMPTZ
);

-- Proctoring & anti-cheating

CREATE TABLE proctoring_sessions (
    id                UUID PRIMARY KEY,
    interview_id      UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    started_at        TIMESTAMPTZ NOT NULL,
    ended_at          TIMESTAMPTZ,
    overall_risk_score NUMERIC(5,2),
    summary           JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE proctoring_events (
    id                  UUID PRIMARY KEY,
    proctoring_session_id UUID NOT NULL REFERENCES proctoring_sessions(id) ON DELETE CASCADE,
    event_type          VARCHAR(64) NOT NULL,
    occurred_at         TIMESTAMPTZ NOT NULL,
    details             JSONB,
    weight              NUMERIC(5,2)
);

CREATE TABLE behavioral_metrics (
    id                  UUID PRIMARY KEY,
    proctoring_session_id UUID NOT NULL REFERENCES proctoring_sessions(id) ON DELETE CASCADE,
    typing_speed_stats  JSONB,
    idle_intervals      JSONB,
    paste_count         INTEGER,
    suspicion_score     NUMERIC(5,2)
);

-- Reporting & integrations

CREATE TABLE interview_reports (
    id            UUID PRIMARY KEY,
    interview_id  UUID NOT NULL REFERENCES interviews(id) ON DELETE CASCADE,
    application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    overall_score NUMERIC(5,2),
    summary       TEXT,
    ai_version    VARCHAR(64),
    generated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    risk_score    NUMERIC(5,2),
    risk_level    VARCHAR(32)
);

CREATE UNIQUE INDEX uq_interview_reports_interview ON interview_reports (interview_id);

CREATE TABLE interview_report_factors (
    id          UUID PRIMARY KEY,
    report_id   UUID NOT NULL REFERENCES interview_reports(id) ON DELETE CASCADE,
    factor_name VARCHAR(64) NOT NULL,
    weight      NUMERIC(5,2),
    score       NUMERIC(5,2),
    max_score   NUMERIC(5,2)
);

CREATE TABLE notifications (
    id             UUID PRIMARY KEY,
    company_id     UUID REFERENCES companies(id) ON DELETE SET NULL,
    user_id        UUID REFERENCES users(id) ON DELETE SET NULL,
    channel        VARCHAR(32) NOT NULL,
    type           VARCHAR(64) NOT NULL,
    template_key   VARCHAR(128),
    payload        JSONB,
    status         VARCHAR(32) NOT NULL,
    sent_at        TIMESTAMPTZ,
    error_message  TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE webhook_events (
    id             UUID PRIMARY KEY,
    company_id     UUID NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    event_type     VARCHAR(64) NOT NULL,
    payload        JSONB,
    target_url     VARCHAR(512) NOT NULL,
    status         VARCHAR(32) NOT NULL,
    last_attempt_at TIMESTAMPTZ,
    retry_count    INTEGER     NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY,
    company_id     UUID REFERENCES companies(id) ON DELETE SET NULL,
    actor_user_id  UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_role     VARCHAR(64),
    action         VARCHAR(128) NOT NULL,
    entity_type    VARCHAR(64)  NOT NULL,
    entity_id      UUID,
    metadata       JSONB,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

