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
    eligibility_status VARCHAR(20) NOT NULL
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

CREATE TABLE IF NOT EXISTS blood_unit_table (
    unit_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    blood_type VARCHAR(5) NOT NULL,
    volume_ml INT NOT NULL,
    collection_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_blood_unit_donor
        FOREIGN KEY (donor_id)
        REFERENCES donor_table(donor_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
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
