-- Ensure full_name exists (idempotent; safe if V3 already applied)
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
