# NGINX 配置使用说明

你现在项目里有两份 Nginx 配置，区别如下：

1. `nginx/nginx.conf`
- 用途：**中间件在 Docker、Gateway 在本地运行**
- 上游地址：`host.docker.internal:8080`
- 适合你当前开发模式

2. `day1/nginx.docker.conf`
- 用途：**全量容器运行（Gateway 也在 Docker）**
- 上游地址：`gateway-service:8080`
- 适合演示或部署镜像联调

## 你当前怎么用（推荐）

你说的是：中间件在 Docker，Java 微服务本地运行。  
所以应该用 `nginx/nginx.conf`。

### 启动步骤

1. 先启动中间件：

```bash
docker compose -f day1/docker-compose.middleware.yml up -d
```

2. 本地启动四个微服务（auth/task/admin/gateway）

3. 准备证书到 `nginx/certs`：
- `server.crt`
- `server.key`

4. 启动 Nginx：

```bash
docker compose up -d nginx
```

### 验证

- 浏览器打开：`https://localhost`
- 接口入口：`https://localhost/api/...`

## 如果不想启用 HTTPS

可以临时改 `nginx/nginx.conf`：

1. 删除 80 到 443 的重定向
2. 在 80 server 里直接 `proxy_pass` 到 Gateway

这样就能用 `http://localhost` 访问。

## 常见问题

1. Nginx 启动失败
- 多数是证书缺失：检查 `nginx/certs/server.crt` 和 `nginx/certs/server.key`

2. 访问 502
- 检查本地 Gateway 是否已启动并监听 `8080`

3. HTTPS 证书不受信任
- 自签名证书是正常现象，开发环境可忽略警告

