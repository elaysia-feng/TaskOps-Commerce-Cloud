# Docker 环境配置指南（中间件版）

适用场景：你在本地频繁改 Java 代码，只把中间件放在 Docker。

## 1. 启动中间件

在项目根目录执行：

```bash
docker compose -f day1/docker-compose.middleware.yml up -d
```

会启动：

1. `nacos`（8848）
2. `mysql`（3306）
3. `redis`（6379）

## 2. 本地运行微服务

分别在本地启动：

```bash
mvn -pl auth-service spring-boot:run
mvn -pl task-service spring-boot:run
mvn -pl admin-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

## 3. 本地服务环境变量（建议）

```bash
NACOS_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=public
TASKOPS_DB_URL=jdbc:mysql://127.0.0.1:3306/internship_taskops?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
TASKOPS_DB_USER=root
TASKOPS_DB_PASS=root
TASKOPS_REDIS_HOST=127.0.0.1
TASKOPS_REDIS_PORT=6379
TASKOPS_JWT_SECRET=1234567890123456789012345678901234567890
TASKOPS_JWT_EXPIRE=86400000
```

## 4. 访问方式

1. 不用 Nginx：直接访问 `http://localhost:8080`
2. 用 Nginx：访问 `https://localhost`（见 `day1/NGINX配置使用说明.md`）

## 5. 关闭中间件

```bash
docker compose -f day1/docker-compose.middleware.yml down
```

