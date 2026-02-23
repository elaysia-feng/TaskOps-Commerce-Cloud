# Spring 启动顺序与配置优先级（TaskOps Cloud 版）

适用版本：你当前项目是 `Spring Boot 2.7.x + Spring Cloud 2021 + Nacos`。

## 1. Spring 启动执行顺序（大流程）

1. `main` 方法启动  
2. 创建 `SpringApplication`，推断应用类型（Web/Reactive）  
3. 准备 `Environment`（先收集系统属性、环境变量、命令行参数等）  
4. 加载早期配置源（`bootstrap.yml`，因为你引入了 `spring-cloud-starter-bootstrap`）  
5. 初始化并连接 Nacos（注册中心/配置中心客户端）  
6. 读取配置数据（本地 `application.yml` + Nacos `*.yaml`）  
7. 创建 `ApplicationContext`  
8. 执行自动装配（AutoConfiguration）  
9. 扫描并注册 Bean  
10. Bean 实例化、依赖注入、`@PostConstruct`  
11. 执行 `CommandLineRunner` / `ApplicationRunner`  
12. Web 容器启动并对外提供服务

## 2. 你项目里的关键“先后点”

1. `bootstrap.yml` 比 `application.yml` 更早  
- 你在 `auth/task/admin/gateway` 都有 `bootstrap.yml`，用于先拿到 Nacos 地址。

2. `spring.config.import` 生效时机  
- `optional:nacos:${spring.application.name}.yaml` 在配置加载阶段引入远程配置。

3. 网关过滤器初始化  
- `JwtGlobalFilter` 是 Bean，容器刷新后生效，请求到来时执行 `filter`。

4. 初始化数据执行时机  
- `CommandLineRunner`（如 `DataInitializer`）在容器启动完成后执行。

## 3. 配置优先级（高 -> 低，常用项）

在 Spring Boot 2.7 中，常见可见优先级可按下面理解：

1. 命令行参数  
- 例：`--server.port=9000`

2. `SPRING_APPLICATION_JSON`

3. Java 系统属性  
- 例：`-Dserver.port=9000`

4. 操作系统环境变量  
- 例：`NACOS_ADDR=192.168.88.100:8848`

5. 配置文件（`application.yml` / `application-*.yml`）  
- profile 专用通常覆盖非 profile  
- 外部文件通常覆盖包内文件

6. `@PropertySource`

7. `SpringApplication.setDefaultProperties` 默认值

## 4. 在你项目里，Nacos 与本地配置谁优先

实操结论（你当前写法）：

1. Nacos 远程配置会作为配置源参与解析  
2. 你如果在 IDEA 里配了环境变量（如 `MW_DB_HOST`），通常会覆盖默认值  
3. 远程配置里如果再引用环境变量占位符（`${MW_DB_HOST:...}`），最终值由运行环境决定

建议记忆：

1. 想临时覆盖：用 IDEA 环境变量/命令行参数  
2. 想团队统一：用 Nacos 配置  
3. 想兜底：放 `application.yml` 默认值

## 5. 你现在最常用的排查顺序

当“配置不生效”时按这个查：

1. 看启动日志是否打印了 Nacos 连接成功  
2. 看是否成功加载了对应 Data ID（如 `auth-service.yaml`）  
3. 看 IDEA 运行配置里是否有同名环境变量覆盖  
4. 看 `bootstrap.yml` 的 `server-addr/namespace` 是否正确  
5. 看 Nacos 命名空间和分组是否匹配（通常 `public + DEFAULT_GROUP`）

## 6. 快速结论

1. 启动顺序上：`bootstrap -> Nacos -> application -> Bean`  
2. 覆盖优先上：`命令行/环境变量` 通常高于 `本地默认配置`  
3. 你现在最佳实践：Nacos 写模板 + 运行时环境变量注入具体地址

