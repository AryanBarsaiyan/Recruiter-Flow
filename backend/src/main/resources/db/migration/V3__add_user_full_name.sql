-- Optional display name for users (e.g. recruiter full name from signup)
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
