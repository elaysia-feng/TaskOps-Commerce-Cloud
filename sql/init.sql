CREATE DATABASE IF NOT EXISTS internship_taskops DEFAULT CHARACTER SET utf8mb4;
USE internship_taskops;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS internship_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    title VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    tech_stack VARCHAR(255),
    priority TINYINT NOT NULL DEFAULT 3,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO',
    progress INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    quality_score INT NOT NULL DEFAULT 60,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_task_created_at(created_at),
    INDEX idx_task_status(status)
);

CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    ip VARCHAR(64),
    success TINYINT NOT NULL,
    message VARCHAR(255),
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    method VARCHAR(255) NOT NULL,
    duration_ms BIGINT NOT NULL,
    success TINYINT NOT NULL,
    error_msg VARCHAR(255),
    created_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS t_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    stock INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_order_user(user_id),
    INDEX idx_order_status(status)
);

CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pay_no VARCHAR(64) NOT NULL UNIQUE,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS t_order_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL UNIQUE,
    routing_key VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_outbox_status_time(status, next_retry_time)
);

CREATE TABLE IF NOT EXISTS t_message_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_message_biz (message_id, biz_type)
);

INSERT INTO t_stock (sku_code, stock, updated_at)
VALUES ('SKU_MEAL_001', 100, NOW()),
       ('SKU_MEAL_002', 80, NOW())
ON DUPLICATE KEY UPDATE stock = VALUES(stock), updated_at = VALUES(updated_at);

