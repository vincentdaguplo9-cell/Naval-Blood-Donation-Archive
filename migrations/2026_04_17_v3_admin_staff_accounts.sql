USE blood_archive;

ALTER TABLE user_table ADD COLUMN password_salt VARCHAR(255) NULL AFTER password_hash;

UPDATE user_table
SET username = CAST(user_id AS CHAR)
WHERE role = 'STAFF'
  AND (username IS NULL OR username = '' OR username LIKE 'staff%');
