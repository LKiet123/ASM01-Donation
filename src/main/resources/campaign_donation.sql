CREATE DATABASE campaign_donation;
USE campaign_donation;

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO roles (name) VALUES ('ADMIN'), ('USER');

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    note VARCHAR(255),
    phone_number VARCHAR(15) UNIQUE NOT NULL,
    address VARCHAR(255),
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    status ENUM('ACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    description TEXT,
    money DECIMAL(15, 2) DEFAULT 0.00,
    status ENUM('NEW', 'IN_PROGRESS', 'ENDED', 'CLOSED') NOT NULL DEFAULT 'NEW',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    donation_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    money DECIMAL(15,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    text TEXT COMMENT 'Optional donation message',
    status ENUM('CONFIRM', 'CONFIRMING', 'CONFIRMED') NOT NULL DEFAULT 'CONFIRMED',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (donation_id) REFERENCES donations(id) ON DELETE CASCADE
);

 INSERT INTO users (full_name, email, phone_number, username, password, role_id, status)
VALUES ('Admin User', 'admin@example.com', '1234567890', 'admin', '$2a$12$abvPXjH3ChvB6zZqOb0dz.I7uY2JLQiLT5MxDbIMz09YOc5IAEBfS', 1, 'ACTIVE');
-- INSERT INTO users (username, password, role_id, email, phone_number)
-- VALUES ('admin', TO_BASE64(AES_ENCRYPT('12345678', '')),1,'admin@example.com','1234567890');

INSERT INTO users (full_name, email, phone_number, username, password, role_id, status)
VALUES ('Admin123', 'admin1234@gmail.com', '0123456987', 'admin1234', TO_BASE64(AES_ENCRYPT('12345678', '')), 1, 'ACTIVE');

INSERT INTO users (full_name, email, phone_number, username, password, role_id, status)
VALUES 
('Nguyễn Văn A', 'nguyenvana@gmail.com', '0905123456', 'nguyenvana', '$2a$12$abvPXjH3ChvB6zZqOb0dz.I7uY2JLQiLT5MxDbIMz09YOc5IAEBfS', 2, 'ACTIVE'),
('Trần Thị B', 'tranthib@yahoo.com', '0912345678', 'tranthib', '$2a$12$abvPXjH3ChvB6zZqOb0dz.I7uY2JLQiLT5MxDbIMz09YOc5IAEBfS', 2, 'ACTIVE');


INSERT INTO donations (code, name, start_date, end_date, organization_name, phone_number, description, money, status)
VALUES 
('QD2025009', 'Quỹ học bổng cho trẻ mồ coi', '2025-07-07', '2025-09-09', 'Hội Liên hiệp phụ nữ', '0341234567', 'Gây quỹ hỗ trợ trẻ em', 500000000.00, 'NEW'),

('QD2025001', 'Ủng hộ miền Trung lũ lụt', '2025-06-01', '2025-07-01', 'Hội Chữ thập đỏ Việt Nam', '0241234567', 'Gây quỹ hỗ trợ bà con vùng lũ miền Trung', 50000000.00, 'IN_PROGRESS');

INSERT INTO user_donations (user_id, donation_id, name, money, text, status)
VALUES 
(3, 1, 'Nguyễn Văn A', 200000.00, 'Chúc bà con sớm vượt qua khó khăn', 'CONFIRMED'),
(4, 1, 'Trần Thị B', 500000.00, 'Một chút tấm lòng gửi đến miền Trung', 'CONFIRMED');




select id, name from roles;
select id, username, password, role_id, status FROM users;
select id, name,  organization_name from donations
