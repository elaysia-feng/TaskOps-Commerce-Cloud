# TaskOps Cloud

当前推荐开发模式：

1. 中间件在 Docker 运行（Nacos/MySQL/Redis）
2. Java 微服务在本地运行（auth/task/admin/order/pay/gateway）
3. Nginx 可选，用于联调或演示

## 架构

`Nginx(80/443) -> Gateway(8080) -> auth(8081) / task(8082) / admin(8083) / order(8084) / pay(8085)`

## 模块说明

- `cloud-common`: 通用返回、异常、上下文、鉴权工具
- `auth-service`: 注册/登录/JWT/内部用户接口
- `task-service`: 任务接口、热榜、缓存防护
- `order-service`: 下单、库存扣减、Outbox 本地消息、订单状态流转
- `pay-service`: 支付单创建、支付回调、支付成功事件发布
- `admin-service`: 看板聚合（登录日志、热榜任务、订单支付摘要）
- `gateway-service`: 统一鉴权与路由

## 启动方式

1. 启动中间件：

```bash
docker compose -f day2/docker-compose.yml up -d
```

2. 本地启动微服务：

```bash
mvn -pl auth-service spring-boot:run
mvn -pl task-service spring-boot:run
mvn -pl order-service spring-boot:run
mvn -pl pay-service spring-boot:run
mvn -pl admin-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

## 交易链路演示

1. 登录获取 token：`POST /api/auth/login`
2. 下单：`POST /api/orders`
3. 模拟支付回调：`POST /api/pay/callback/mock`
4. 查订单：`GET /api/orders/{orderNo}`
5. 查管理看板：`GET /api/admin/dashboard`
