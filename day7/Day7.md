# Day7 - Seata 分布式事务建表设计（结合当前项目）

## 1. 目标
基于当前 `springBoot` 多模块项目，按微服务职责拆分数据库，并补齐 Seata AT 模式学习所需表结构。

当前涉及业务服务：
- `auth-service`
- `task-service`
- `order-service`
- `pay-service`

网关与聚合服务：
- `gateway-service`（无业务持久化）
- `admin-service`（无业务持久化）

## 2. 库设计（推荐）
- `taskops_auth`：认证与用户
- `taskops_task`：任务
- `taskops_order`：订单与库存
- `taskops_pay`：支付
- `taskops_seata`：Seata Server（TC）元数据

## 3. 各服务需要的业务表

### auth-service -> taskops_auth
- `sys_user`
- `sys_role`
- `sys_user_role`
- `login_log`

### task-service -> taskops_task
- `internship_task`

### order-service -> taskops_order
- `t_stock`
- `t_order`
- `t_order_outbox`
- `t_message_consume_log`

### pay-service -> taskops_pay
- `t_payment`
- `t_message_consume_log`

### admin-service / gateway-service
- 不需要新增业务表

## 4. Seata 必需表
- 每个参与全局事务的业务库都需要：`undo_log`
- Seata Server 独立库 `taskops_seata` 需要：
  - `global_table`
  - `branch_table`
  - `lock_table`
  - `distributed_lock`

## 5. SQL 脚本
完整可执行脚本见：
- `day7/seata-init.sql`

