# 当前项目 Redis 使用点说明

## 1. auth-service：登录风控

代码位置：`auth-service/src/main/java/com/elias/auth/service/impl/AuthAppServiceImpl.java`

- 登录前检查是否被临时锁定  
  键：`auth:lock:{username}`  
  逻辑：存在则拒绝登录（`account temporary locked`）

- 登录失败计数  
  键：`auth:fail:{username}`  
  逻辑：`INCR` 自增，`EXPIRE 10分钟`

- 失败次数达到阈值后加锁  
  键：`auth:lock:{username}`  
  逻辑：失败次数 `>= 5` 时写入，`TTL 5分钟`

- 登录成功后清理失败计数  
  键：`auth:fail:{username}`  
  逻辑：删除失败计数键

说明：
- 项目没有把“用户名/密码”写入 Redis。
- Redis 在 auth-service 主要用于风控（失败计数与临时锁定）。

## 2. task-service：任务热榜缓存

代码位置：`task-service/src/main/java/com/elias/task/service/impl/TaskAppServiceImpl.java`

- 查看任务详情时更新热度  
  键：`task:hot:zset`（ZSet）  
  逻辑：`ZADD taskId score`

- 查询热榜时优先读缓存  
  键：`task:hot:zset`  
  逻辑：`ZREVRANGE 0..9`，为空则触发重建

- 定时重建热榜（每 5 分钟）  
  键：`task:hot:zset`  
  逻辑：先删除旧键，再批量写入最新 Top 数据

## 3. 当前 Redis 连接配置

- `auth-service/src/main/resources/application.yml`
- `task-service/src/main/resources/application.yml`

默认读取：
- Host：`${TASKOPS_REDIS_HOST:${TASKOPS_MW_HOST:192.168.88.100}}`
- Port：`${TASKOPS_REDIS_PORT:6380}`

