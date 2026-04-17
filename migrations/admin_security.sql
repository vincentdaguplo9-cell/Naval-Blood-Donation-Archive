-- Idempotent-ish: each statement can be ignored if column already exists.
ALTER TABLE admin_table ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE admin_table ADD COLUMN password_salt VARCHAR(255);
ALTER TABLE admin_table ADD COLUMN failed_attempts INT DEFAULT 0;
ALTER TABLE admin_table ADD COLUMN lock_until DATETIME NULL;
