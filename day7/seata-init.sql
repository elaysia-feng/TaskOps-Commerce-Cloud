-- Day7: Seata + 微服务分库建表（MySQL 8 / utf8mb4）

-- =========================
-- 1) 创建数据库
-- =========================
CREATE DATABASE IF NOT EXISTS taskops_auth DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS taskops_task DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS taskops_order DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS taskops_pay DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS taskops_seata DEFAULT CHARACTER SET utf8mb4;

-- =========================
-- 2) auth-service 库
-- =========================
USE taskops_auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL UNIQUE,
    role_name VARCHAR(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    ip VARCHAR(64),
    success TINYINT NOT NULL,
    message VARCHAR(255),
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME NOT NULL,
  log_modified DATETIME NOT NULL,
  ext VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 3) task-service 库
-- =========================
USE taskops_task;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME NOT NULL,
  log_modified DATETIME NOT NULL,
  ext VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 4) order-service 库
-- =========================
USE taskops_order;

CREATE TABLE IF NOT EXISTS t_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku_code VARCHAR(64) NOT NULL UNIQUE,
    stock INT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_message_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_message_biz (message_id, biz_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME NOT NULL,
  log_modified DATETIME NOT NULL,
  ext VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO t_stock (sku_code, stock, updated_at)
VALUES ('SKU_MEAL_001', 100, NOW()),
       ('SKU_MEAL_002', 80, NOW())
ON DUPLICATE KEY UPDATE stock = VALUES(stock), updated_at = VALUES(updated_at);

-- =========================
-- 5) pay-service 库
-- =========================
USE taskops_pay;

CREATE TABLE IF NOT EXISTS t_payment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pay_no VARCHAR(64) NOT NULL UNIQUE,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_message_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL,
    biz_type VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_message_biz (message_id, biz_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME NOT NULL,
  log_modified DATETIME NOT NULL,
  ext VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 6) Seata Server（TC）库
-- =========================
USE taskops_seata;

CREATE TABLE IF NOT EXISTS global_table (
  xid VARCHAR(128) NOT NULL,
  transaction_id BIGINT,
  status TINYINT NOT NULL,
  application_id VARCHAR(32),
  transaction_service_group VARCHAR(32),
  transaction_name VARCHAR(128),
  timeout INT,
  begin_time BIGINT,
  application_data VARCHAR(2000),
  gmt_create DATETIME,
  gmt_modified DATETIME,
  PRIMARY KEY (xid),
  KEY idx_status_gmt_modified (status, gmt_modified),
  KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS branch_table (
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  transaction_id BIGINT,
  resource_group_id VARCHAR(32),
  resource_id VARCHAR(256),
  branch_type VARCHAR(8),
  status TINYINT,
  client_id VARCHAR(64),
  application_data VARCHAR(2000),
  gmt_create DATETIME,
  gmt_modified DATETIME,
  PRIMARY KEY (branch_id),
  KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lock_table (
  row_key VARCHAR(128) NOT NULL,
  xid VARCHAR(128),
  transaction_id BIGINT,
  branch_id BIGINT NOT NULL,
  resource_id VARCHAR(256),
  table_name VARCHAR(32),
  pk VARCHAR(36),
  status TINYINT NOT NULL DEFAULT 0,
  gmt_create DATETIME,
  gmt_modified DATETIME,
  PRIMARY KEY (row_key),
  KEY idx_status (status),
  KEY idx_branch_id (branch_id),
  KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS distributed_lock (
  lock_key CHAR(20) NOT NULL,
  lock_value VARCHAR(20) NOT NULL,
  expire BIGINT,
  PRIMARY KEY (lock_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('AsyncCommitting', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('RetryCommitting', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('RetryRollbacking', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('TxTimeoutCheck', ' ', 0);

