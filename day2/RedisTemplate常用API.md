# RedisTemplate / StringRedisTemplate 常用 API（结合当前项目）

## 1. 两个模板的区别

- `RedisTemplate<K, V>`：通用模板，可存对象；需要关注序列化配置。
- `StringRedisTemplate`：`RedisTemplate<String, String>` 的特化版，Key/Value 都按字符串处理，最常用、最稳妥。

你当前项目主要使用：`StringRedisTemplate`。  
代码参考：
- `auth-service/src/main/java/com/elias/auth/service/impl/AuthAppServiceImpl.java`
- `task-service/src/main/java/com/elias/task/service/impl/TaskAppServiceImpl.java`

## 2. Key 级别操作

### 2.1 `hasKey`
```java
Boolean exists = redisTemplate.hasKey("auth:lock:admin");
```
- 作用：判断 key 是否存在
- 返回：`Boolean`

### 2.2 `delete`
```java
redisTemplate.delete("auth:fail:admin");
```
- 作用：删除单个 key

### 2.3 `expire`
```java
redisTemplate.expire("auth:fail:admin", Duration.ofMinutes(10));
```
- 作用：给 key 设置过期时间（TTL）
- 返回：`Boolean`，表示是否设置成功

## 3. String（Value）操作：`opsForValue`

### 3.1 `set`
```java
redisTemplate.opsForValue().set("auth:lock:admin", "1", Duration.ofMinutes(5));
```
- 作用：写字符串值，可带过期时间

### 3.2 `get`
```java
String val = redisTemplate.opsForValue().get("auth:lock:admin");
```
- 作用：读取字符串值

### 3.3 `increment`
```java
Long count = redisTemplate.opsForValue().increment("auth:fail:admin");
```
- 作用：数值自增（原值必须是可转数字字符串）
- 返回：自增后的值
- 当前项目用于登录失败计数

## 4. ZSet（有序集合）操作：`opsForZSet`

### 4.1 `add`
```java
redisTemplate.opsForZSet().add("task:hot:zset", "1001", 98.5);
```
- 作用：按分数写入成员
- 返回：`Boolean`，是否新增成功（更新已有成员分数通常返回 `false`）

### 4.2 `reverseRange`
```java
Set<String> top10 = redisTemplate.opsForZSet().reverseRange("task:hot:zset", 0, 9);
```
- 作用：按分数从高到低取区间
- 当前项目用于热榜 Top10

## 5. 其他常见结构（项目暂未使用）

## 5.1 Hash：`opsForHash`
```java
redisTemplate.opsForHash().put("user:1", "nickname", "seele");
Object nickname = redisTemplate.opsForHash().get("user:1", "nickname");
```
- 场景：对象字段缓存

## 5.2 List：`opsForList`
```java
redisTemplate.opsForList().leftPush("queue:task", "job-1");
String job = redisTemplate.opsForList().rightPop("queue:task");
```
- 场景：简单队列

## 5.3 Set：`opsForSet`
```java
redisTemplate.opsForSet().add("online:users", "u1", "u2");
Set<String> users = redisTemplate.opsForSet().members("online:users");
```
- 场景：去重集合

## 6. 实战注意点

- 登录风控类 key 一定要配 `expire`，防止永久锁定。
- `increment` 的 key 不要混存非数字。
- 热榜用 ZSet 时，member 推荐用业务主键（如 taskId）。
- 需要跨语言/可读性优先时，优先 `StringRedisTemplate`。
- 若用 `RedisTemplate` 存对象，必须统一序列化方案（如 JSON），避免乱码和反序列化问题。

