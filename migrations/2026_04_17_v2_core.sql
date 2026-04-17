-- V2 core schema upgrades for more realistic blood bank workflows.
USE blood_archive;

-- Users + roles (future use; current login still uses admin_table).
CREATE TABLE IF NOT EXISTS user_table (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','STAFF','VIEWER') NOT NULL DEFAULT 'STAFF',
    full_name VARCHAR(120),
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Donation sessions (one donor visit).
CREATE TABLE IF NOT EXISTS donation_session_table (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    collected_at DATETIME NOT NULL,
    collected_by INT NOT NULL,
    site VARCHAR(120),
    notes VARCHAR(255),
    CONSTRAINT fk_session_donor FOREIGN KEY (donor_id) REFERENCES donor_table(donor_id),
    CONSTRAINT fk_session_user FOREIGN KEY (collected_by) REFERENCES user_table(user_id)
);

-- Screening record for a donation session.
CREATE TABLE IF NOT EXISTS screening_table (
    screening_id INT PRIMARY KEY AUTO_INCREMENT,
    session_id INT NOT NULL UNIQUE,
    weight_kg DECIMAL(5,2),
    bp_systolic INT,
    bp_diastolic INT,
    hemoglobin_g_dl DECIMAL(4,1),
    temperature_c DECIMAL(4,1),
    passed TINYINT(1) NOT NULL,
    failure_reason VARCHAR(255),
    CONSTRAINT fk_screening_session FOREIGN KEY (session_id) REFERENCES donation_session_table(session_id)
);

-- Unit lifecycle additions.
ALTER TABLE blood_unit_table
    ADD COLUMN session_id INT NULL,
    ADD COLUMN unit_code VARCHAR(30) NULL UNIQUE,
    ADD COLUMN component ENUM('WB','PRBC','PLT','FFP') NOT NULL DEFAULT 'WB',
    ADD COLUMN test_status ENUM('PENDING','PASSED','FAILED') NOT NULL DEFAULT 'PENDING',
    ADD COLUMN storage_location VARCHAR(60) NULL,
    ADD CONSTRAINT fk_unit_session FOREIGN KEY (session_id) REFERENCES donation_session_table(session_id);

-- Lab testing per unit.
CREATE TABLE IF NOT EXISTS lab_test_table (
    test_id INT PRIMARY KEY AUTO_INCREMENT,
    unit_id INT NOT NULL,
    tested_at DATETIME NOT NULL,
    tested_by INT NOT NULL,
    hiv ENUM('NEG','POS','NA') NOT NULL DEFAULT 'NA',
    hbv ENUM('NEG','POS','NA') NOT NULL DEFAULT 'NA',
    hcv ENUM('NEG','POS','NA') NOT NULL DEFAULT 'NA',
    syphilis ENUM('NEG','POS','NA') NOT NULL DEFAULT 'NA',
    malaria ENUM('NEG','POS','NA') NOT NULL DEFAULT 'NA',
    overall ENUM('PASS','FAIL') NOT NULL,
    remarks VARCHAR(255),
    CONSTRAINT fk_test_unit FOREIGN KEY (unit_id) REFERENCES blood_unit_table(unit_id),
    CONSTRAINT fk_test_user FOREIGN KEY (tested_by) REFERENCES user_table(user_id)
);

-- Inventory transaction log (traceability).
CREATE TABLE IF NOT EXISTS inventory_tx_table (
    tx_id INT PRIMARY KEY AUTO_INCREMENT,
    unit_id INT NOT NULL,
    tx_type ENUM('COLLECT','TEST_PASS','TEST_FAIL','RESERVE','ISSUE','DISCARD','EXPIRE') NOT NULL,
    tx_at DATETIME NOT NULL,
    performed_by INT NOT NULL,
    reference_no VARCHAR(60),
    remarks VARCHAR(255),
    CONSTRAINT fk_invtx_unit FOREIGN KEY (unit_id) REFERENCES blood_unit_table(unit_id),
    CONSTRAINT fk_invtx_user FOREIGN KEY (performed_by) REFERENCES user_table(user_id)
);

-- Optional audit log (fine for school demos, good for realism).
CREATE TABLE IF NOT EXISTS audit_log_table (
    audit_id INT PRIMARY KEY AUTO_INCREMENT,
    at_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id INT NOT NULL,
    action VARCHAR(60) NOT NULL,
    entity VARCHAR(40) NOT NULL,
    entity_id INT NULL,
    details VARCHAR(255),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES user_table(user_id)
);

-- Seed a default user record for existing demo flow (admin).
INSERT INTO user_table (username, password_hash, role, full_name)
SELECT 'admin', 'demo', 'ADMIN', 'System Admin'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE username = 'admin');

-- Seed placeholder staff IDs used by existing seed transactions (201..205).
INSERT INTO user_table (user_id, username, password_hash, role, full_name)
SELECT 201, 'staff201', 'demo', 'STAFF', 'Staff 201'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = 201);

INSERT INTO user_table (user_id, username, password_hash, role, full_name)
SELECT 202, 'staff202', 'demo', 'STAFF', 'Staff 202'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = 202);

INSERT INTO user_table (user_id, username, password_hash, role, full_name)
SELECT 203, 'staff203', 'demo', 'STAFF', 'Staff 203'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = 203);

INSERT INTO user_table (user_id, username, password_hash, role, full_name)
SELECT 204, 'staff204', 'demo', 'STAFF', 'Staff 204'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = 204);

INSERT INTO user_table (user_id, username, password_hash, role, full_name)
SELECT 205, 'staff205', 'demo', 'STAFF', 'Staff 205'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE user_id = 205);
