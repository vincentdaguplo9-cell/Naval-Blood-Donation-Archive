-- Naval Blood Donation Archive System database schema
CREATE DATABASE IF NOT EXISTS blood_archive;
USE blood_archive;

CREATE TABLE IF NOT EXISTS donor_table (
    donor_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    blood_type VARCHAR(5) NOT NULL,
    contact_no VARCHAR(15),
    address VARCHAR(100),
    last_donation_date DATE,
    eligibility_status VARCHAR(20) NOT NULL,
    -- V2 fields (realism)
    sex ENUM('M','F') NULL,
    birth_date DATE NULL,
    email VARCHAR(120) NULL,
    deferred_until DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admin_table (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100),
    password_hash VARCHAR(255),
    password_salt VARCHAR(255),
    failed_attempts INT DEFAULT 0,
    lock_until DATETIME NULL
);

-- V2: track schema updates applied by the app.
CREATE TABLE IF NOT EXISTS schema_migrations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    migration VARCHAR(200) NOT NULL UNIQUE,
    applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V2: users + roles (future expansion; used by sessions/lab/inventory logs).
CREATE TABLE IF NOT EXISTS user_table (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255),
    role ENUM('ADMIN','STAFF','VIEWER') NOT NULL DEFAULT 'STAFF',
    full_name VARCHAR(120),
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V2: donation session (one donor visit).
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

-- V2: screening per session.
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

CREATE TABLE IF NOT EXISTS blood_unit_table (
    unit_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    blood_type VARCHAR(5) NOT NULL,
    volume_ml INT NOT NULL,
    collection_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    -- V2 fields (traceability + lab workflow)
    session_id INT NULL,
    unit_code VARCHAR(30) NULL UNIQUE,
    component ENUM('WB','PRBC','PLT','FFP') NOT NULL DEFAULT 'WB',
    test_status ENUM('PENDING','PASSED','FAILED') NOT NULL DEFAULT 'PENDING',
    storage_location VARCHAR(60) NULL,
    CONSTRAINT fk_blood_unit_donor
        FOREIGN KEY (donor_id)
        REFERENCES donor_table(donor_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
    ,
    CONSTRAINT fk_blood_unit_session
        FOREIGN KEY (session_id)
        REFERENCES donation_session_table(session_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE INDEX idx_unit_status ON blood_unit_table(status);
CREATE INDEX idx_unit_test_status ON blood_unit_table(test_status);
CREATE INDEX idx_unit_expiry ON blood_unit_table(expiry_date);

-- V2: lab test results (TTI screening).
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

-- V2: inventory transactions for traceability (collect/test/issue/etc).
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

-- V2: optional audit log for accountability.
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

CREATE TABLE IF NOT EXISTS donation_transaction_table (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    unit_id INT NOT NULL,
    staff_id INT NOT NULL,
    transaction_date DATETIME NOT NULL,
    remarks VARCHAR(100),
    CONSTRAINT fk_tx_donor
        FOREIGN KEY (donor_id)
        REFERENCES donor_table(donor_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_tx_unit
        FOREIGN KEY (unit_id)
        REFERENCES blood_unit_table(unit_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

INSERT INTO admin_table (username, password)
SELECT 'admin', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM admin_table WHERE username = 'admin');

-- Seed V2 placeholder users for demo/staff_id usage.
INSERT INTO user_table (username, password_hash, role, full_name)
SELECT 'admin', 'demo', 'ADMIN', 'System Admin'
WHERE NOT EXISTS (SELECT 1 FROM user_table WHERE username = 'admin');

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
