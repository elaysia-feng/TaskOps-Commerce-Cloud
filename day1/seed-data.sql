USE internship_taskops;

-- =============================
-- Seed Data for TaskOps
-- 可重复执行（主键冲突时会更新）
-- =============================

-- 1) 角色
INSERT INTO sys_role (id, role_code, role_name)
VALUES
    (1, 'ADMIN', 'ADMIN_ROLE'),
    (2, 'USER', 'USER_ROLE')
ON DUPLICATE KEY UPDATE
    role_name = VALUES(role_name);

-- 2) 用户
-- 注意：password 这里是演示占位，不保证可登录。
-- 如果要可登录用户，建议直接调 /api/auth/register 创建。
INSERT INTO sys_user (id, username, password, nickname, status, created_at, updated_at)
VALUES
    (1, 'admin', '$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghijklmn', 'super_admin', 1, NOW(), NOW()),
    (2, 'elias', '$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghijklmn', 'elias_dev', 1, NOW(), NOW()),
    (3, 'intern_a', '$2a$10$abcdefghijklmnopqrstuv1234567890abcdefghijklmn', 'intern_a', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    status = VALUES(status),
    updated_at = VALUES(updated_at);

-- 3) 用户-角色关系
INSERT IGNORE INTO sys_user_role (user_id, role_id)
VALUES
    (1, 1),
    (1, 2),
    (2, 2),
    (3, 2);

-- 4) 任务数据
INSERT INTO internship_task
    (id, owner_id, title, description, tech_stack, priority, status, progress, comment_count, quality_score, created_at, updated_at)
VALUES
    (1, 2, '重构鉴权模块', '将网关鉴权抽离并补齐异常处理、日志追踪。', 'Spring Cloud Gateway, JWT, Redis', 1, 'IN_PROGRESS', 55, 12, 92, NOW(), NOW()),
    (2, 2, '任务检索优化', '补充索引并优化分页查询性能，减少慢 SQL。', 'MySQL, MyBatis-Plus', 2, 'TODO', 10, 5, 85, NOW(), NOW()),
    (3, 3, '管理看板联调', '联调 admin-service 聚合接口并验证权限链路。', 'OpenFeign, Nacos', 3, 'DONE', 100, 8, 88, NOW(), NOW()),
    (4, 3, '限流策略压测', '验证 Nginx + Gateway 双层限流效果。', 'Nginx, JMeter', 2, 'IN_PROGRESS', 40, 6, 80, NOW(), NOW()),
    (5, 2, '日志规范整理', '统一 access/error/application 日志字段规范。', 'Nginx, SLF4J', 4, 'TODO', 0, 1, 70, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    tech_stack = VALUES(tech_stack),
    priority = VALUES(priority),
    status = VALUES(status),
    progress = VALUES(progress),
    comment_count = VALUES(comment_count),
    quality_score = VALUES(quality_score),
    updated_at = VALUES(updated_at);

-- 5) 登录日志
INSERT INTO login_log (id, username, ip, success, message, created_at)
VALUES
    (1, 'admin', '192.168.88.10', 1, 'success', NOW()),
    (2, 'elias', '192.168.88.11', 1, 'success', NOW()),
    (3, 'intern_a', '192.168.88.12', 0, 'wrong password', NOW()),
    (4, 'intern_a', '192.168.88.12', 0, 'wrong password', NOW()),
    (5, 'intern_a', '192.168.88.12', 1, 'success', NOW())
ON DUPLICATE KEY UPDATE
    message = VALUES(message),
    created_at = VALUES(created_at);

-- 6) 操作日志
INSERT INTO operation_log (id, operator, action, method, duration_ms, success, error_msg, created_at)
VALUES
    (1, 'admin', 'dashboard_view', 'AdminController.dashboard()', 32, 1, NULL, NOW()),
    (2, 'elias', 'task_create', 'TaskController.create()', 48, 1, NULL, NOW()),
    (3, 'elias', 'task_search', 'TaskController.search()', 23, 1, NULL, NOW()),
    (4, 'intern_a', 'task_detail', 'TaskController.detail()', 17, 1, NULL, NOW()),
    (5, 'intern_a', 'task_create', 'TaskController.create()', 12, 0, 'validation error', NOW())
ON DUPLICATE KEY UPDATE
    operator = VALUES(operator),
    action = VALUES(action),
    method = VALUES(method),
    duration_ms = VALUES(duration_ms),
    success = VALUES(success),
    error_msg = VALUES(error_msg),
    created_at = VALUES(created_at);

