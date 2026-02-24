# Repository Guidelines

## 项目结构与模块组织
本仓库是一个多模块 Maven 项目（根目录 `pom.xml`），用于 TaskOps Cloud 微服务开发。

- `cloud-common/`：公共代码（统一返回、异常、鉴权工具、上下文、AOP）。
- `auth-service/`：注册、登录、JWT 签发、内部用户接口。
- `task-service/`：任务创建/检索/热榜等核心业务。
- `admin-service/`：管理端聚合接口（通过 Feign 调用其他服务）。
- `gateway-service/`：网关路由、JWT 全局过滤与鉴权控制。
- `sql/init.sql`：数据库初始化脚本。
- `nginx/`：本地反向代理配置与静态资源。
- `day1/`、`day2/`：环境搭建、compose 与排错文档。

源码路径：`*/src/main/java`，资源路径：`*/src/main/resources`。

## 构建、测试与本地开发命令
- `mvn clean install -DskipTests`：构建全部模块并产出 JAR。
- `mvn -pl auth-service -am spring-boot:run`：启动指定服务（模块名可替换）。
- `mvn -pl cloud-common,auth-service,task-service,admin-service -am -DskipTests compile`：核心模块快速编译校验。
- `docker compose -f day2/docker-compose.yml up -d`：启动中间件（Nacos/MySQL/Redis）。

## 代码风格与命名规范
- Java 11，4 空格缩进，UTF-8 编码。
- 包名以 `com.elias.*` 为基准。
- 类命名遵循：`*Controller`、`*Service`、`*ServiceImpl`、`*Mapper`、`*Config`。
- 优先使用构造器注入（可配合 `@RequiredArgsConstructor`）。
- 业务异常统一使用 `ErrorCode + BizException`，避免散落魔法数字。

## 测试规范
使用 Maven 测试生命周期：
- `mvn test`（全量）或 `mvn -pl <module> test`（单模块）。

新增测试时建议：
- 放在 `src/test/java`；
- 单元测试命名 `*Test`，集成测试命名 `*IT`；
- 覆盖服务层逻辑、错误码分支与关键控制器流程。

## 提交与合并请求规范
当前提交历史较短（如 `day1`、`day2`），建议采用清晰、按模块分域的提交信息，例如：
- `auth: fix login lock key handling`
- `common: add TimeLimit aspect`

PR 至少包含：
1. 变更摘要与影响模块；
2. 已执行的构建/验证命令；
3. 配置或库表影响（`sql/`、compose、环境变量）；
4. 接口行为变化（请求/响应示例）。

## 安全与配置建议
- 禁止提交密钥与敏感信息（JWT 密钥、数据库密码、Nacos 凭据）。
- 优先使用环境变量（`TASKOPS_*`），避免硬编码 IP/端口。
- 使用 ThreadLocal 上下文时，务必在过滤器 `finally` 中清理，避免线程复用导致数据串请求。
