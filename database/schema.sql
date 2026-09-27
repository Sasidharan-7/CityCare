-- ==========================================================
-- CityCare: AI-Powered Smart City Issue Management
-- Database Schema (MySQL)
-- Problem: SIH25031 - Crowdsourced Civic Issue Reporting
-- ==========================================================

CREATE DATABASE IF NOT EXISTS citycare_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE citycare_db;

-- ----------------------------------------------------------
-- 1. Departments Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 2. Users Table (Citizens, Officers, Admins)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CITIZEN', -- 'CITIZEN', 'OFFICER', 'ADMIN'
    department_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) 
        REFERENCES departments(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 3. Complaints Table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(30) NOT NULL UNIQUE, -- e.g. CC1001
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,           -- POTHOLE, GARBAGE, WATER_LEAKAGE, BROKEN_STREETLIGHT, OPEN_DRAIN, ROAD_DAMAGE, OTHER
    description TEXT NOT NULL,
    image_url VARCHAR(500),
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- CRITICAL, HIGH, MEDIUM, LOW
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    department_id BIGINT,
    officer_id BIGINT,
    resolution_proof_url VARCHAR(500),
    resolution_remarks TEXT,
    is_possible_duplicate BOOLEAN DEFAULT FALSE,
    primary_complaint_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaints_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_complaints_department FOREIGN KEY (department_id) 
        REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_complaints_officer FOREIGN KEY (officer_id) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 4. Complaint Updates / Audit History
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS complaint_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks TEXT,
    updated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_updates_complaint FOREIGN KEY (complaint_id) 
        REFERENCES complaints(id) ON DELETE CASCADE,
    CONSTRAINT fk_updates_user FOREIGN KEY (updated_by) 
        REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- 5. In-App Notifications
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    complaint_id BIGINT,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_complaint FOREIGN KEY (complaint_id) 
        REFERENCES complaints(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Seed Initial Departments
-- ----------------------------------------------------------
INSERT INTO departments (name, code, description) VALUES
('Road Department', 'ROAD', 'Handles potholes, road asphalt damages, and surface cracks'),
('Sanitation Department', 'SANITATION', 'Handles solid waste accumulation, garbage clearance, and street cleanliness'),
('Water Department', 'WATER', 'Handles water pipe leakages, main line bursts, and drinking water supply issues'),
('Electricity Department', 'ELECTRICITY', 'Handles broken streetlights, exposed power lines, and electrical hazards'),
('Public Works Department', 'PWD', 'Handles open drains, uncovered manholes, stormwater ditches, and footpaths'),
('General Civic Department', 'CIVIC', 'Handles general civic amenities and miscellaneous civic issues')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ----------------------------------------------------------
-- Seed Initial Demo Users (Passwords are BCrypt hashed: 'password123')
-- Hash: $2a$10$7Rk8jZ0nUv0ZlE2tN8CaeuUj5.M19pG2F9Pq2n7a.5aTjQf5pWkeK
-- ----------------------------------------------------------
INSERT INTO users (name, email, phone, password, role, department_id) VALUES
('Admin Officer', 'admin@citycare.gov.in', '9876543210', '$2a$10$7Rk8jZ0nUv0ZlE2tN8CaeuUj5.M19pG2F9Pq2n7a.5aTjQf5pWkeK', 'ADMIN', NULL),
('Rajesh Kumar (Road Dept)', 'officer.road@citycare.gov.in', '9876543211', '$2a$10$7Rk8jZ0nUv0ZlE2tN8CaeuUj5.M19pG2F9Pq2n7a.5aTjQf5pWkeK', 'OFFICER', 1),
('Sunita Sharma (Sanitation)', 'officer.sanitation@citycare.gov.in', '9876543212', '$2a$10$7Rk8jZ0nUv0ZlE2tN8CaeuUj5.M19pG2F9Pq2n7a.5aTjQf5pWkeK', 'OFFICER', 2),
('Arun Patel (Citizen)', 'citizen@gmail.com', '9876543213', '$2a$10$7Rk8jZ0nUv0ZlE2tN8CaeuUj5.M19pG2F9Pq2n7a.5aTjQf5pWkeK', 'CITIZEN', NULL)
ON DUPLICATE KEY UPDATE email=VALUES(email);
