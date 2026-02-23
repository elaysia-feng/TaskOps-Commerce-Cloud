# Redis 五种数据类型对应的数据形式

## 1. String（字符串）

- 数据形式：`key -> value`（一个键对应一个字符串值）
- 典型值：文本、数字、JSON 字符串
- Java API：`opsForValue()`
- 常见命令：`SET` / `GET` / `INCR`

示例：
```text
auth:lock:seele -> "1"
auth:fail:seele -> "3"
```

## 2. Hash（哈希）

- 数据形式：`key -> { field: value, field: value }`
- 典型值：一个对象的多个属性
- Java API：`opsForHash()`
- 常见命令：`HSET` / `HGET` / `HGETALL`

示例：
```text
user:4 -> { username: "seele", nickname: "Seele", status: "1" }
```

## 3. List（列表）

- 数据形式：`key -> [v1, v2, v3 ...]`（有序、可重复）
- 典型值：消息队列、时间线
- Java API：`opsForList()`
- 常见命令：`LPUSH` / `RPUSH` / `LPOP` / `RPOP` / `LRANGE`

示例：
```text
queue:task -> ["job3", "job2", "job1"]
```

## 4. Set（集合）

- 数据形式：`key -> {v1, v2, v3}`（无序、去重）
- 典型值：标签集合、在线用户集合
- Java API：`opsForSet()`
- 常见命令：`SADD` / `SMEMBERS` / `SREM`

示例：
```text
online:users -> {"u1", "u2", "u3"}
```

## 5. ZSet（有序集合）

- 数据形式：`key -> { member: score }`（按 score 排序、member 去重）
- 典型值：排行榜、热度榜
- Java API：`opsForZSet()`
- 常见命令：`ZADD` / `ZREVRANGE` / `ZRANGE`

示例：
```text
task:hot:zset -> { "101": 98.5, "88": 91.0, "77": 85.0 }
```

## 当前项目实际使用

- `String`：登录失败计数、临时锁定（auth-service）
- `ZSet`：任务热榜（task-service）

## 什么时候用什么类型

- 用 `String`：
  存单值、计数器、开关位、短期状态最直接。  
  例如：登录失败次数、验证码、分布式锁标记。

- 用 `Hash`：
  存“一个对象的多个字段”更合适，更新单个字段成本低。  
  例如：用户资料、商品基础信息。

- 用 `List`：
  需要“有顺序、可重复、两端进出”的数据。  
  例如：简单消息队列、最近浏览记录。

- 用 `Set`：
  需要“去重集合、成员判断、交并差集”时。  
  例如：在线用户集合、标签集合、共同好友。

- 用 `ZSet`：
  需要“可排序 + 排行榜 + TopN + 区间查询”时优先。  
  例如：热榜、积分榜、按时间/热度排序的数据。

## 快速选型口诀

- 单值/计数：`String`
- 对象字段：`Hash`
- 队列顺序：`List`
- 去重集合：`Set`
- 排名排序：`ZSet`
