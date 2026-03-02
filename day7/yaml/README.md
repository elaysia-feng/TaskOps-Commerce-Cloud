# Seata YAML Usage

## Files
- `auth-service-seata.yml`
- `task-service-seata.yml`
- `order-service-seata.yml`
- `pay-service-seata.yml`
- `seata-server-application.yml`

## How to apply
1. For service configs:
   - Option A: merge content into each service `src/main/resources/application.yml`.
   - Option B: publish to Nacos with Data ID:
     - `auth-service.yaml`
     - `task-service.yaml`
     - `order-service.yaml`
     - `pay-service.yaml`
2. For Seata Server:
   - Use `seata-server-application.yml` as `seata-server/conf/application.yml`.
3. SQL init:
   - Execute `day7/seata-init.sql` first (includes `undo_log` and TC tables).

## Required dependency (each RM/TM service)
Add Seata starter in `auth-service`, `task-service`, `order-service`, `pay-service`:

```xml
<dependency>
    <groupId>io.seata</groupId>
    <artifactId>seata-spring-boot-starter</artifactId>
    <version>1.7.1</version>
</dependency>
```

## Note
- Start with AT mode.
- Put `@GlobalTransactional` on the global transaction entry method (usually in `order-service` orchestration flow).

