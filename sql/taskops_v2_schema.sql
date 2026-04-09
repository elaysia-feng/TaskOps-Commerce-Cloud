-- TaskOps v2 库表设计稿
-- 目标：替换当前练习型表结构，形成更适合长期演进的平台化设计。
-- 说明：
-- 1. 本文件不会直接替换当前正在运行的旧表结构。
-- 2. 本文件是后续重构使用的目标库表设计。
-- 3. 数据库按领域拆分，跨库关联仅保留逻辑关系。

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS taskops_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS taskops_member DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS taskops_task DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS taskops_trade DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS taskops_wallet DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS taskops_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =========================================================
-- taskops_auth
-- =========================================================
USE taskops_auth;

CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_no VARCHAR(32) NOT NULL COMMENT '用户编号',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别',
    mobile VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    mobile_verified TINYINT NOT NULL DEFAULT 0 COMMENT '手机号是否已验证',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    email_verified TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱是否已验证',
    real_name VARCHAR(64) DEFAULT NULL COMMENT '真实姓名',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    register_source VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '注册来源',
    password_set TINYINT NOT NULL DEFAULT 1 COMMENT '是否已设置密码',
    last_login_at DATETIME DEFAULT NULL COMMENT '最近登录时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最近登录IP',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_user_no (user_no),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_mobile (mobile),
    UNIQUE KEY uk_email (email),
    KEY idx_status_created (status, created_at)
) COMMENT='用户主表';

CREATE TABLE IF NOT EXISTS user_auth_identity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    identity_type VARCHAR(32) NOT NULL COMMENT '身份类型',
    principal VARCHAR(128) NOT NULL COMMENT '登录标识',
    credential_hash VARCHAR(255) DEFAULT NULL COMMENT '凭证摘要',
    credential_version INT NOT NULL DEFAULT 1 COMMENT '凭证版本',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    last_verified_at DATETIME DEFAULT NULL COMMENT '最近验证时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_identity (identity_type, principal),
    KEY idx_identity_user (user_id)
) COMMENT='用户登录身份表';

CREATE TABLE IF NOT EXISTS auth_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_code VARCHAR(64) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(64) NOT NULL COMMENT '角色名称',
    role_scope VARCHAR(32) NOT NULL DEFAULT 'PLATFORM' COMMENT '角色范围',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) COMMENT='角色定义表';

CREATE TABLE IF NOT EXISTS auth_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS auth_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    session_no VARCHAR(32) NOT NULL COMMENT '会话编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    refresh_token_hash VARCHAR(128) NOT NULL COMMENT '刷新令牌摘要',
    access_token_jti VARCHAR(64) DEFAULT NULL COMMENT '访问令牌JTI',
    client_type VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '客户端类型',
    device_id VARCHAR(64) DEFAULT NULL COMMENT '设备ID',
    device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    ip VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    user_agent VARCHAR(255) DEFAULT NULL COMMENT '用户代理',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    expired_at DATETIME NOT NULL COMMENT '过期时间',
    revoked_at DATETIME DEFAULT NULL COMMENT '吊销时间',
    last_seen_at DATETIME DEFAULT NULL COMMENT '最近活跃时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_session_no (session_no),
    UNIQUE KEY uk_refresh_token_hash (refresh_token_hash),
    KEY idx_session_user_status (user_id, status)
) COMMENT='登录会话表';

CREATE TABLE IF NOT EXISTS auth_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID',
    username VARCHAR(64) DEFAULT NULL COMMENT '用户名',
    login_type VARCHAR(32) NOT NULL COMMENT '登录类型',
    success TINYINT NOT NULL COMMENT '是否成功',
    ip VARCHAR(64) DEFAULT NULL COMMENT 'IP地址',
    device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    user_agent VARCHAR(255) DEFAULT NULL COMMENT '用户代理',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_login_user_created (user_id, created_at),
    KEY idx_login_username_created (username, created_at)
) COMMENT='登录日志表';

INSERT INTO auth_role (role_code, role_name, role_scope, status, created_at, updated_at)
VALUES ('USER', 'User', 'PLATFORM', 'ACTIVE', NOW(), NOW()),
       ('ADMIN', 'Admin', 'ADMIN', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name), updated_at = VALUES(updated_at);

-- =========================================================
-- taskops_member
-- =========================================================
USE taskops_member;

CREATE TABLE IF NOT EXISTS member_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    plan_code VARCHAR(64) NOT NULL COMMENT '套餐编码',
    plan_name VARCHAR(64) NOT NULL COMMENT '套餐名称',
    level_code VARCHAR(32) NOT NULL COMMENT '等级编码',
    billing_cycle VARCHAR(32) NOT NULL COMMENT '计费周期',
    sale_price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    currency_code CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
    task_publish_quota INT NOT NULL DEFAULT 10 COMMENT '任务发布额度',
    priority_weight INT NOT NULL DEFAULT 0 COMMENT 'Priority Weight',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    description VARCHAR(255) DEFAULT NULL COMMENT '描述',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_plan_code (plan_code),
    KEY idx_level_status (level_code, status)
) COMMENT='会员套餐表';

CREATE TABLE IF NOT EXISTS user_membership (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    membership_no VARCHAR(32) NOT NULL COMMENT '会员编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    level_code VARCHAR(32) NOT NULL COMMENT '等级编码',
    plan_code VARCHAR(64) NOT NULL COMMENT '套餐编码',
    source_order_no VARCHAR(32) DEFAULT NULL COMMENT '来源订单号',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    auto_renew TINYINT NOT NULL DEFAULT 0 COMMENT '是否自动续费',
    start_at DATETIME NOT NULL COMMENT '开始时间',
    end_at DATETIME NOT NULL COMMENT '结束时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_membership_no (membership_no),
    KEY idx_membership_user_status (user_id, status, end_at),
    KEY idx_membership_order (source_order_no)
) COMMENT='用户会员权益表';

CREATE TABLE IF NOT EXISTS member_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    membership_no VARCHAR(32) DEFAULT NULL COMMENT '会员编号',
    change_type VARCHAR(32) NOT NULL COMMENT '变更类型',
    old_level_code VARCHAR(32) DEFAULT NULL COMMENT '原等级编码',
    new_level_code VARCHAR(32) DEFAULT NULL COMMENT '新等级编码',
    source_order_no VARCHAR(32) DEFAULT NULL COMMENT '来源订单号',
    operator_type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '操作人类型',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_member_change_user_created (user_id, created_at),
    KEY idx_member_change_order (source_order_no)
) COMMENT='会员变更流水表';

INSERT INTO member_plan (
    plan_code, plan_name, level_code, billing_cycle, sale_price, original_price, currency_code,
    task_publish_quota, priority_weight, status, sort_no, description, created_at, updated_at
)
VALUES
    ('VIP_MONTHLY', 'VIP Monthly', 'VIP', 'MONTH', 19.90, 19.90, 'CNY', 50, 10, 'ACTIVE', 10, 'Monthly VIP plan', NOW(), NOW()),
    ('SVIP_MONTHLY', 'SVIP Monthly', 'SVIP', 'MONTH', 39.90, 39.90, 'CNY', 100, 20, 'ACTIVE', 20, 'Monthly SVIP plan', NOW(), NOW()),
    ('VIP_YEARLY', 'VIP Yearly', 'VIP', 'YEAR', 199.00, 238.80, 'CNY', 50, 10, 'ACTIVE', 30, 'Yearly VIP plan', NOW(), NOW()),
    ('SVIP_YEARLY', 'SVIP Yearly', 'SVIP', 'YEAR', 399.00, 478.80, 'CNY', 100, 20, 'ACTIVE', 40, 'Yearly SVIP plan', NOW(), NOW())
ON DUPLICATE KEY UPDATE plan_name = VALUES(plan_name), sale_price = VALUES(sale_price), updated_at = VALUES(updated_at);

-- =========================================================
-- taskops_task
-- =========================================================
USE taskops_task;

CREATE TABLE IF NOT EXISTS task_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(64) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT NULL COMMENT 'Parent ID',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_parent_sort (parent_id, sort_no)
) COMMENT='任务分类表';

CREATE TABLE IF NOT EXISTS task_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_no VARCHAR(32) NOT NULL COMMENT '任务编号',
    publisher_id BIGINT NOT NULL COMMENT '发布者用户ID',
    current_assignee_id BIGINT DEFAULT NULL COMMENT '当前接单人ID',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    description TEXT NOT NULL COMMENT '描述',
    tags_json JSON DEFAULT NULL COMMENT '标签JSON',
    location_mode VARCHAR(32) NOT NULL DEFAULT 'ONLINE' COMMENT '地点模式',
    location_text VARCHAR(255) DEFAULT NULL COMMENT '地点描述',
    contact_name VARCHAR(64) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    contact_wechat VARCHAR(64) DEFAULT NULL COMMENT '联系微信',
    reward_amount DECIMAL(10,2) NOT NULL COMMENT '赏金金额',
    platform_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台服务费金额',
    settlement_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '结算金额',
    escrow_order_no VARCHAR(32) DEFAULT NULL COMMENT '托管订单号',
    proof_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否要求凭证',
    priority_level TINYINT NOT NULL DEFAULT 3 COMMENT 'Priority Level',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    deadline DATETIME DEFAULT NULL COMMENT '截止时间',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    submission_round INT NOT NULL DEFAULT 0 COMMENT '提交轮次',
    last_submission_at DATETIME DEFAULT NULL COMMENT '最近提交时间',
    accepted_at DATETIME DEFAULT NULL COMMENT '接单时间',
    approved_at DATETIME DEFAULT NULL COMMENT '审核通过时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    cancelled_at DATETIME DEFAULT NULL COMMENT '取消时间',
    close_reason VARCHAR(255) DEFAULT NULL COMMENT '关闭原因',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_publisher_status_created (publisher_id, status, created_at),
    KEY idx_assignee_status_created (current_assignee_id, status, created_at),
    KEY idx_task_status_deadline (status, deadline),
    KEY idx_task_category_status (category_id, status)
) COMMENT='任务主表';

CREATE TABLE IF NOT EXISTS task_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    assignment_no VARCHAR(32) NOT NULL COMMENT '接单记录编号',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_no VARCHAR(32) NOT NULL COMMENT '任务编号',
    publisher_id BIGINT NOT NULL COMMENT '发布者用户ID',
    assignee_id BIGINT NOT NULL COMMENT '接单人ID',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    accepted_at DATETIME NOT NULL COMMENT '接单时间',
    cancelled_at DATETIME DEFAULT NULL COMMENT '取消时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    cancel_reason VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_assignment_no (assignment_no),
    KEY idx_assignment_task (task_id),
    KEY idx_assignment_assignee_status (assignee_id, status)
) COMMENT='任务接单履约表';

CREATE TABLE IF NOT EXISTS task_submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    submission_no VARCHAR(32) NOT NULL COMMENT '提交单号',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_no VARCHAR(32) NOT NULL COMMENT '任务编号',
    assignment_id BIGINT NOT NULL COMMENT '接单记录ID',
    submit_user_id BIGINT NOT NULL COMMENT '提交人用户ID',
    round_no INT NOT NULL COMMENT '轮次',
    content TEXT NOT NULL COMMENT '内容',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    reviewed_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    reviewed_at DATETIME DEFAULT NULL COMMENT '审核时间',
    submitted_at DATETIME NOT NULL COMMENT '提交时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_submission_no (submission_no),
    UNIQUE KEY uk_task_round (task_id, round_no),
    KEY idx_submission_task_status (task_id, status, submitted_at),
    KEY idx_submission_user_created (submit_user_id, created_at)
) COMMENT='任务提交表';

CREATE TABLE IF NOT EXISTS task_submission_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    storage_provider VARCHAR(32) NOT NULL DEFAULT 'ALIYUN_OSS' COMMENT '存储服务商',
    bucket_name VARCHAR(64) NOT NULL COMMENT '存储桶名称',
    object_key VARCHAR(255) NOT NULL COMMENT '对象键',
    file_url VARCHAR(512) NOT NULL COMMENT '文件访问URL',
    original_name VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    content_type VARCHAR(128) DEFAULT NULL COMMENT '内容类型',
    size_bytes BIGINT NOT NULL DEFAULT 0 COMMENT '文件大小字节数',
    sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_submission_object (submission_id, object_key),
    KEY idx_attachment_task (task_id),
    KEY idx_attachment_submission_sort (submission_id, sort_no)
) COMMENT='任务提交材料表';

CREATE TABLE IF NOT EXISTS task_settlement_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    request_no VARCHAR(32) NOT NULL COMMENT '请求编号',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    submission_id BIGINT NOT NULL COMMENT '提交ID',
    publisher_id BIGINT NOT NULL COMMENT '发布者用户ID',
    assignee_id BIGINT NOT NULL COMMENT '接单人ID',
    escrow_order_no VARCHAR(32) NOT NULL COMMENT '托管订单号',
    gross_amount DECIMAL(10,2) NOT NULL COMMENT '毛额',
    platform_fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '平台服务费金额',
    net_amount DECIMAL(10,2) NOT NULL COMMENT '净额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    message_id VARCHAR(64) DEFAULT NULL COMMENT '消息ID',
    settled_at DATETIME DEFAULT NULL COMMENT '结算完成时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_request_no (request_no),
    UNIQUE KEY uk_task_submission (task_id, submission_id),
    KEY idx_settlement_assignee_status (assignee_id, status),
    KEY idx_settlement_order (escrow_order_no)
) COMMENT='任务结算请求表';

CREATE TABLE IF NOT EXISTS task_action_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    task_no VARCHAR(32) NOT NULL COMMENT '任务编号',
    action_type VARCHAR(32) NOT NULL COMMENT 'CREATE/PUBLISH/ACCEPT/SUBMIT/APPROVE/REJECT/CANCEL/SETTLE/EXPIRE',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_role VARCHAR(32) DEFAULT NULL COMMENT '操作人角色',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    ext_json JSON DEFAULT NULL COMMENT '扩展JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_task_action_created (task_id, created_at),
    KEY idx_task_action_type_created (action_type, created_at)
) COMMENT='任务操作日志表';

INSERT INTO task_category (category_code, category_name, parent_id, sort_no, status, created_at, updated_at)
VALUES ('DELIVERY', 'Delivery', NULL, 10, 'ACTIVE', NOW(), NOW()),
       ('DESIGN', 'Design', NULL, 20, 'ACTIVE', NOW(), NOW()),
       ('DEVELOPMENT', 'Development', NULL, 30, 'ACTIVE', NOW(), NOW()),
       ('WRITING', 'Writing', NULL, 40, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE category_name = VALUES(category_name), updated_at = VALUES(updated_at);

-- =========================================================
-- taskops_trade
-- =========================================================
USE taskops_trade;

CREATE TABLE IF NOT EXISTS trade_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    buyer_user_id BIGINT NOT NULL COMMENT '购买方用户ID',
    seller_user_id BIGINT DEFAULT NULL COMMENT '卖方用户ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
    order_title VARCHAR(128) NOT NULL COMMENT '订单标题',
    order_desc VARCHAR(255) DEFAULT NULL COMMENT '订单描述',
    currency_code CHAR(3) NOT NULL DEFAULT 'CNY' COMMENT '币种编码',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    payable_amount DECIMAL(10,2) NOT NULL COMMENT '应付金额',
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    close_reason VARCHAR(255) DEFAULT NULL COMMENT '关闭原因',
    expired_at DATETIME DEFAULT NULL COMMENT '过期时间',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    closed_at DATETIME DEFAULT NULL COMMENT '关闭时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_trade_buyer_status_created (buyer_user_id, status, created_at),
    KEY idx_trade_biz (biz_type, biz_no),
    KEY idx_trade_status_expired (status, expired_at)
) COMMENT='交易订单表';

CREATE TABLE IF NOT EXISTS trade_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    item_type VARCHAR(32) NOT NULL COMMENT '条目类型',
    item_code VARCHAR(64) DEFAULT NULL COMMENT '条目编码',
    item_name VARCHAR(128) NOT NULL COMMENT '条目名称',
    biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型',
    biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    item_snapshot_json JSON DEFAULT NULL COMMENT '条目快照JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    KEY idx_order_item_order (order_id),
    KEY idx_order_item_type_code (item_type, item_code)
) COMMENT='交易订单明细表';

CREATE TABLE IF NOT EXISTS payment_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    pay_no VARCHAR(32) NOT NULL COMMENT '支付单号',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    buyer_user_id BIGINT NOT NULL COMMENT '购买方用户ID',
    channel VARCHAR(32) NOT NULL COMMENT '渠道',
    pay_scene VARCHAR(32) NOT NULL DEFAULT 'WEB' COMMENT '支付场景',
    subject VARCHAR(128) NOT NULL COMMENT '主题',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    third_trade_no VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    buyer_account VARCHAR(128) DEFAULT NULL COMMENT '付款方账号',
    pay_url VARCHAR(512) DEFAULT NULL COMMENT '支付链接',
    pay_params_json JSON DEFAULT NULL COMMENT '支付参数JSON',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    callback_content TEXT DEFAULT NULL COMMENT '回调内容',
    expired_at DATETIME DEFAULT NULL COMMENT '过期时间',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    callback_at DATETIME DEFAULT NULL COMMENT '回调时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_pay_no (pay_no),
    KEY idx_payment_order_no (order_no),
    KEY idx_payment_user_status (buyer_user_id, status, created_at),
    KEY idx_payment_third_trade_no (third_trade_no)
) COMMENT='支付单表';

CREATE TABLE IF NOT EXISTS payment_callback_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    pay_no VARCHAR(32) NOT NULL COMMENT '支付单号',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    channel VARCHAR(32) NOT NULL COMMENT '渠道',
    notify_id VARCHAR(128) DEFAULT NULL COMMENT '通知ID',
    callback_status VARCHAR(32) NOT NULL COMMENT '回调状态',
    callback_body TEXT NOT NULL COMMENT '回调报文',
    processed_result VARCHAR(255) DEFAULT NULL COMMENT '处理结果',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_callback_pay_created (pay_no, created_at),
    KEY idx_callback_notify_id (notify_id)
) COMMENT='支付回调日志表';

CREATE TABLE IF NOT EXISTS refund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    refund_no VARCHAR(32) NOT NULL COMMENT '退款单号',
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    pay_no VARCHAR(32) NOT NULL COMMENT '支付单号',
    buyer_user_id BIGINT NOT NULL COMMENT '购买方用户ID',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    refund_reason VARCHAR(255) DEFAULT NULL COMMENT '退款原因',
    third_refund_no VARCHAR(64) DEFAULT NULL COMMENT '第三方退款单号',
    callback_content TEXT DEFAULT NULL COMMENT '回调内容',
    refunded_at DATETIME DEFAULT NULL COMMENT '退款完成时间',
    callback_at DATETIME DEFAULT NULL COMMENT '回调时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_refund_no (refund_no),
    KEY idx_refund_order (order_no),
    KEY idx_refund_pay (pay_no),
    KEY idx_refund_user_status (buyer_user_id, status)
) COMMENT='退款单表';

CREATE TABLE IF NOT EXISTS trade_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_no VARCHAR(32) NOT NULL COMMENT '业务单号',
    topic VARCHAR(64) NOT NULL COMMENT '主题标识',
    routing_key VARCHAR(64) NOT NULL COMMENT '路由键',
    payload_json JSON NOT NULL COMMENT '消息载荷JSON',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    next_retry_at DATETIME DEFAULT NULL COMMENT '下次重试时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_trade_message_id (message_id),
    KEY idx_trade_outbox_status_retry (status, next_retry_at)
) COMMENT='交易出站消息表';

CREATE TABLE IF NOT EXISTS trade_message_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    consumer_name VARCHAR(64) NOT NULL COMMENT '消费者名称',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_trade_consume (message_id, consumer_name),
    KEY idx_trade_consume_biz (biz_type, created_at)
) COMMENT='交易消费幂等表';

-- =========================================================
-- taskops_wallet
-- =========================================================
USE taskops_wallet;

CREATE TABLE IF NOT EXISTS wallet_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    wallet_no VARCHAR(32) NOT NULL COMMENT '钱包编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    available_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
    frozen_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    total_income DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计收入金额',
    total_expense DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计支出金额',
    total_withdraw DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计提现金额',
    total_refund DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计退款金额',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',
    UNIQUE KEY uk_wallet_no (wallet_no),
    UNIQUE KEY uk_wallet_user (user_id)
) COMMENT='钱包账户表';

CREATE TABLE IF NOT EXISTS wallet_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    flow_no VARCHAR(32) NOT NULL COMMENT '流水号',
    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    change_type VARCHAR(32) NOT NULL COMMENT '变更类型',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
    direction VARCHAR(16) NOT NULL COMMENT '方向',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    balance_before DECIMAL(12,2) NOT NULL COMMENT '变更前余额',
    balance_after DECIMAL(12,2) NOT NULL COMMENT '变更后余额',
    frozen_before DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '变更前冻结金额',
    frozen_after DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '变更后冻结金额',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    ext_json JSON DEFAULT NULL COMMENT '扩展JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_flow_no (flow_no),
    KEY idx_wallet_ledger_wallet_created (wallet_id, created_at),
    KEY idx_wallet_ledger_user_biz (user_id, biz_type, created_at)
) COMMENT='钱包流水表';

CREATE TABLE IF NOT EXISTS wallet_freeze_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    freeze_no VARCHAR(32) NOT NULL COMMENT '冻结单号',
    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    biz_no VARCHAR(32) DEFAULT NULL COMMENT '业务单号',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    released_at DATETIME DEFAULT NULL COMMENT '释放时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_freeze_no (freeze_no),
    KEY idx_freeze_wallet_status (wallet_id, status),
    KEY idx_freeze_user_biz (user_id, biz_type, biz_no)
) COMMENT='钱包冻结记录表';

CREATE TABLE IF NOT EXISTS withdraw_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_type VARCHAR(32) NOT NULL COMMENT '账户类型',
    account_name VARCHAR(64) NOT NULL COMMENT '账户名称',
    account_no_cipher VARCHAR(255) NOT NULL COMMENT '账户号密文',
    account_no_masked VARCHAR(64) NOT NULL COMMENT '账户号脱敏值',
    bank_name VARCHAR(128) DEFAULT NULL COMMENT '银行名称',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    KEY idx_withdraw_account_user_status (user_id, status)
) COMMENT='提现账户表';

CREATE TABLE IF NOT EXISTS withdraw_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    withdraw_no VARCHAR(32) NOT NULL COMMENT '提现单号',
    wallet_id BIGINT NOT NULL COMMENT '钱包ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    freeze_no VARCHAR(32) DEFAULT NULL COMMENT '冻结单号',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    fee_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '手续费金额',
    net_amount DECIMAL(12,2) NOT NULL COMMENT '净额',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    reject_reason VARCHAR(255) DEFAULT NULL COMMENT '驳回原因',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_at DATETIME DEFAULT NULL COMMENT '审核时间',
    paid_at DATETIME DEFAULT NULL COMMENT '支付时间',
    remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
    account_snapshot_json JSON DEFAULT NULL COMMENT '账户快照JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_withdraw_no (withdraw_no),
    KEY idx_withdraw_user_status (user_id, status, created_at),
    KEY idx_withdraw_wallet_status (wallet_id, status)
) COMMENT='提现申请表';

CREATE TABLE IF NOT EXISTS wallet_message_consume_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    message_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    consumer_name VARCHAR(64) NOT NULL COMMENT '消费者名称',
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_wallet_consume (message_id, consumer_name)
) COMMENT='钱包消费幂等表';

-- =========================================================
-- taskops_ai
-- =========================================================
USE taskops_ai;

CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    session_no VARCHAR(32) NOT NULL COMMENT '会话编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT DEFAULT NULL COMMENT '任务ID',
    title VARCHAR(128) DEFAULT NULL COMMENT '标题',
    scene_code VARCHAR(32) NOT NULL DEFAULT 'GENERAL' COMMENT '场景编码',
    model_name VARCHAR(64) DEFAULT NULL COMMENT '模型名称',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    last_message_at DATETIME DEFAULT NULL COMMENT '最近消息时间',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_ai_session_no (session_no),
    KEY idx_ai_conversation_user_status (user_id, status, updated_at),
    KEY idx_ai_conversation_task (task_id)
) COMMENT='AI会话表';

CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role_code VARCHAR(16) NOT NULL COMMENT '角色编码',
    content MEDIUMTEXT NOT NULL COMMENT '内容',
    token_count INT DEFAULT NULL COMMENT 'Token数量',
    message_status VARCHAR(32) NOT NULL DEFAULT 'DONE' COMMENT '消息状态',
    ext_json JSON DEFAULT NULL COMMENT '扩展JSON',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_ai_message_conversation_created (conversation_id, created_at)
) COMMENT='AI消息表';

CREATE TABLE IF NOT EXISTS ai_memory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    memory_type VARCHAR(32) NOT NULL COMMENT '记忆类型',
    title VARCHAR(128) DEFAULT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    importance_score TINYINT NOT NULL DEFAULT 50 COMMENT '重要度分',
    source_message_id BIGINT DEFAULT NULL COMMENT '来源消息ID',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    updated_at DATETIME NOT NULL COMMENT '更新时间',
    KEY idx_ai_memory_user_type (user_id, memory_type),
    KEY idx_ai_memory_user_status (user_id, status)
) COMMENT='AI长期记忆表';

CREATE TABLE IF NOT EXISTS ai_conversation_memory_rel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    memory_id BIGINT NOT NULL COMMENT '记忆ID',
    selected TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    UNIQUE KEY uk_conversation_memory (conversation_id, memory_id)
) COMMENT='会话记忆关联表';

CREATE TABLE IF NOT EXISTS ai_tool_call_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    message_id BIGINT DEFAULT NULL COMMENT '消息ID',
    tool_name VARCHAR(64) NOT NULL COMMENT '工具名称',
    request_json JSON DEFAULT NULL COMMENT '请求JSON',
    response_json JSON DEFAULT NULL COMMENT '响应JSON',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    error_message VARCHAR(255) DEFAULT NULL COMMENT '错误信息',
    created_at DATETIME NOT NULL COMMENT '创建时间',
    KEY idx_ai_tool_conversation_created (conversation_id, created_at),
    KEY idx_ai_tool_name_status (tool_name, status)
) COMMENT='AI工具调用日志表';

-- =========================================================
-- seata undo_log in business databases
-- =========================================================
USE taskops_member;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚镜像信息',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    ext VARCHAR(100) DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT回滚日志表';

USE taskops_task;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚镜像信息',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    ext VARCHAR(100) DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT回滚日志表';

USE taskops_trade;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚镜像信息',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    ext VARCHAR(100) DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT回滚日志表';

USE taskops_wallet;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚镜像信息',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    ext VARCHAR(100) DEFAULT NULL COMMENT '扩展字段',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT回滚日志表';

-- =========================================================
-- taskops_seata
-- =========================================================
CREATE DATABASE IF NOT EXISTS taskops_seata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE taskops_seata;

CREATE TABLE IF NOT EXISTS global_table (
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    transaction_id BIGINT COMMENT '事务ID',
    status TINYINT NOT NULL COMMENT '事务状态',
    application_id VARCHAR(32) COMMENT '应用ID',
    transaction_service_group VARCHAR(32) COMMENT '事务分组',
    transaction_name VARCHAR(128) COMMENT '事务名称',
    timeout INT COMMENT '超时时间毫秒',
    begin_time BIGINT COMMENT '开始时间戳',
    application_data VARCHAR(2000) COMMENT '应用扩展数据',
    gmt_create DATETIME COMMENT '创建时间',
    gmt_modified DATETIME COMMENT '修改时间',
    PRIMARY KEY (xid),
    KEY idx_status_gmt_modified (status, gmt_modified),
    KEY idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata全局事务表';

CREATE TABLE IF NOT EXISTS branch_table (
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务XID',
    transaction_id BIGINT COMMENT '事务ID',
    resource_group_id VARCHAR(32) COMMENT '资源组ID',
    resource_id VARCHAR(256) COMMENT '资源ID',
    branch_type VARCHAR(8) COMMENT '分支类型',
    status TINYINT COMMENT '分支状态',
    client_id VARCHAR(64) COMMENT '客户端ID',
    application_data VARCHAR(2000) COMMENT '应用扩展数据',
    gmt_create DATETIME COMMENT '创建时间',
    gmt_modified DATETIME COMMENT '修改时间',
    PRIMARY KEY (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata分支事务表';

CREATE TABLE IF NOT EXISTS lock_table (
    row_key VARCHAR(128) NOT NULL COMMENT '行锁主键',
    xid VARCHAR(128) COMMENT '全局事务XID',
    transaction_id BIGINT COMMENT '事务ID',
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    resource_id VARCHAR(256) COMMENT '资源ID',
    table_name VARCHAR(32) COMMENT '表名',
    pk VARCHAR(36) COMMENT '主键值',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '锁状态',
    gmt_create DATETIME COMMENT '创建时间',
    gmt_modified DATETIME COMMENT '修改时间',
    PRIMARY KEY (row_key),
    KEY idx_status (status),
    KEY idx_branch_id (branch_id),
    KEY idx_xid (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata全局锁表';

CREATE TABLE IF NOT EXISTS distributed_lock (
    lock_key CHAR(20) NOT NULL COMMENT '锁键',
    lock_value VARCHAR(20) NOT NULL COMMENT '锁值',
    expire BIGINT COMMENT '过期时间',
    PRIMARY KEY (lock_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata分布式锁表';

INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('AsyncCommitting', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('RetryCommitting', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('RetryRollbacking', ' ', 0);
INSERT IGNORE INTO distributed_lock(lock_key, lock_value, expire) VALUES ('TxTimeoutCheck', ' ', 0);