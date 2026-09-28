-- ======================================================================
-- Smart Library Resource Management & Automated Fine Calculator
-- Complete Database Schema and Sample Data Seed Script
-- Database: MySQL 8.0+
-- ======================================================================

CREATE DATABASE IF NOT EXISTS smart_library;
USE smart_library;

-- 1. Fine Rules Table
CREATE TABLE IF NOT EXISTS fine_rule (
    rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fine_per_day DOUBLE NOT NULL DEFAULT 5.00,
    grace_period INT NOT NULL DEFAULT 0,
    maximum_fine DOUBLE NOT NULL DEFAULT 100.00,
    membership_type VARCHAR(50) DEFAULT 'ALL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 2. Book Table
CREATE TABLE IF NOT EXISTS book (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    publisher VARCHAR(150),
    publication_year INT,
    quantity INT NOT NULL DEFAULT 1,
    available_quantity INT NOT NULL DEFAULT 1,
    shelf_number VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Member Table
CREATE TABLE IF NOT EXISTS member (
    member_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    department VARCHAR(100),
    membership_type VARCHAR(50) DEFAULT 'Student',
    registration_date DATE DEFAULT (CURRENT_DATE),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
);

-- 4. Borrow Table
CREATE TABLE IF NOT EXISTS borrow (
    borrow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    CONSTRAINT fk_borrow_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE
);

-- 5. Fine Table
CREATE TABLE IF NOT EXISTS fine (
    fine_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    overdue_days INT NOT NULL DEFAULT 0,
    fine_amount DOUBLE NOT NULL DEFAULT 0.00,
    fine_date DATE NOT NULL,
    payment_date DATE,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_fine_borrow FOREIGN KEY (borrow_id) REFERENCES borrow(borrow_id) ON DELETE CASCADE,
    CONSTRAINT fk_fine_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
);

-- 6. Reservation Table
CREATE TABLE IF NOT EXISTS reservation (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_book FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE
);

-- 7. Maintenance Table
CREATE TABLE IF NOT EXISTS maintenance (
    maintenance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    expected_completion_date DATE,
    actual_completion_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'UNDER_MAINTENANCE',
    remarks TEXT,
    CONSTRAINT fk_maint_book FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE CASCADE
);

-- 8. Alert Table
CREATE TABLE IF NOT EXISTS alert (
    alert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    book_id BIGINT,
    alert_type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    CONSTRAINT fk_alert_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL,
    CONSTRAINT fk_alert_book FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE SET NULL
);

-- 9. Library Transaction Table
CREATE TABLE IF NOT EXISTS library_transaction (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_type VARCHAR(50) NOT NULL,
    member_id BIGINT,
    book_id BIGINT,
    description VARCHAR(500) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    CONSTRAINT fk_tx_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE SET NULL,
    CONSTRAINT fk_tx_book FOREIGN KEY (book_id) REFERENCES book(book_id) ON DELETE SET NULL
);
