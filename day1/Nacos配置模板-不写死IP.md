# Nacos 配置模板（不写死 IP）

目标：配置里不写死 `192.168.x.x`，通过环境变量适配不同机器。

## 使用方式

在 Nacos `public` 命名空间（默认分组 `DEFAULT_GROUP`）创建以下 Data ID：

1. `auth-service.yaml`
2. `task-service.yaml`
3. `admin-service.yaml`
4. `gateway-service.yaml`

## 1) auth-service.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MW_DB_HOST:127.0.0.1}:${MW_DB_PORT:3306}/internship_taskops?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: ${MW_DB_USER:root}
    password: ${MW_DB_PASS:root}
  redis:
    host: ${MW_REDIS_HOST:127.0.0.1}
    port: ${MW_REDIS_PORT:6379}

app:
  jwt:
    secret: ${MW_JWT_SECRET:change-me-please}
    expire-ms: ${MW_JWT_EXPIRE:86400000}
```

## 2) task-service.yaml

```yaml
spring:
  datasource:
    url: jdbc:mysql://${MW_DB_HOST:127.0.0.1}:${MW_DB_PORT:3306}/internship_taskops?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: ${MW_DB_USER:root}
    password: ${MW_DB_PASS:root}
  redis:
    host: ${MW_REDIS_HOST:127.0.0.1}
    port: ${MW_REDIS_PORT:6379}
```

## 3) admin-service.yaml

```yaml
logging:
  level:
    root: INFO
```

## 4) gateway-service.yaml

```yaml
app:
  jwt:
    secret: ${MW_JWT_SECRET:change-me-please}
```

## 本地运行时环境变量示例（Windows/IDE 都可配）

```bash
MW_DB_HOST=192.168.88.100
MW_DB_PORT=3307
MW_DB_USER=root
MW_DB_PASS=root
MW_REDIS_HOST=192.168.88.100
MW_REDIS_PORT=6380
MW_JWT_SECRET=1234567890123456789012345678901234567890
MW_JWT_EXPIRE=86400000
```

## 说明

1. 这样做后，同一份 Nacos 配置可复用多环境（开发/测试/生产）。
2. 迁移机器时只改环境变量，不改 Nacos 文本。
3. `change-me-please` 仅为默认占位，实际请配置真实密钥。

