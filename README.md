# TaskOps Cloud

当前推荐开发模式：

1. 中间件在 Docker 运行（Nacos/MySQL/Redis）
2. Java 微服务在本地运行（auth/task/admin/gateway）
3. Nginx 可选，建议在联调或演示时启用

## 架构

`Nginx(80/443) -> Gateway(8080) -> auth(8081) / task(8082) / admin(8083)`

## 模块说明

- `cloud-common`: 通用返回、异常、上下文
- `auth-service`: 注册/登录/JWT/内部用户接口
- `task-service`: 任务接口、热榜、Feign 调 auth
- `admin-service`: 看板聚合、Feign 调 auth/task
- `gateway-service`: 统一鉴权与路由

## 启动方式

1. 启动中间件：

```bash
docker compose -f day1/docker-compose.middleware.yml up -d
```

2. 本地启动微服务：

```bash
mvn -pl auth-service spring-boot:run
mvn -pl task-service spring-boot:run
mvn -pl admin-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

3. 可选启用 Nginx（反代到本地 Gateway）：

```bash
docker compose up -d nginx
```

## 文档入口

- Docker 中间件指南：`day1/Docker环境配置指南.md`
- Nginx 配置说明：`day1/NGINX配置使用说明.md`
- 流程图：`day1/项目流程图.md`
