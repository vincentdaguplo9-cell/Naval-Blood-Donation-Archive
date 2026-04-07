ALTER TABLE admin_table
    ADD COLUMN password_hash VARCHAR(255),
    ADD COLUMN password_salt VARCHAR(255),
    ADD COLUMN failed_attempts INT DEFAULT 0,
    ADD COLUMN lock_until DATETIME NULL;
