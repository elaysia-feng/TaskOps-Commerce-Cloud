# day9 - SpringBoot 项目接入 RabbitMQ（简单版）

目标：先在你现有微服务里跑通最基础的 `simple queue`，做到“一个服务发消息，另一个服务收消息”。

建议演练模块：
- 生产者：`order-service`
- 消费者：`task-service`
- 队列名：`simple.queue`

## 1. 加依赖（两个服务都要）
在这两个模块的 `pom.xml` 里都加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

如果父工程已经统一引入，也可以不重复加；没有就按上面加到模块里最稳。

## 2. 加 RabbitMQ 连接配置

分别在：
- `order-service/src/main/resources/application.yml`
- `task-service/src/main/resources/application.yml`

增加（或合并）下面配置：

```yml
spring:
  rabbitmq:
    host: 192.168.88.100
    port: 5672
    virtual-host: /eliasccccc
    username: elias
    password: 1234
```

> 注意：`virtual-host`、账号密码必须和 RabbitMQ 管理台一致。

## 3. 生产者代码（order-service）

新建：`order-service/src/main/java/com/elias/order/mq/SimpleMessageProducer.java`

```java
package com.elias.order.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimpleMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(String message) {
        // 使用默认交换机，routingKey=队列名
        rabbitTemplate.convertAndSend("simple.queue", message);
    }
}
```

为了方便你马上验证，再建一个测试接口：

`order-service/src/main/java/com/elias/order/controller/MqTestController.java`

```java
package com.elias.order.controller;

import com.elias.order.mq.SimpleMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MqTestController {

    private final SimpleMessageProducer simpleMessageProducer;

    @GetMapping("/mq/send")
    public String send(@RequestParam(defaultValue = "hello mq") String msg) {
        simpleMessageProducer.send(msg);
        return "sent: " + msg;
    }
}
```

## 4. 消费者代码（task-service）

新建：`task-service/src/main/java/com/elias/task/mq/SimpleMessageListener.java`

```java
package com.elias.task.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleMessageListener {

    @RabbitListener(queues = "simple.queue")
    public void onMessage(String message) {
        log.info("[task-service] 收到MQ消息: {}", message);
    }
}
```

## 5. 队列准备（RabbitMQ 管理台）

在管理台创建队列：
- Name: `simple.queue`
- Type: `Classic`（或者 `Default for virtual host`，前提是 vhost 默认就是 classic）
- Durable: 打开

这个简单模式不需要手动建交换机，`convertAndSend(queueName, msg)` 走默认交换机。

## 6. 运行和验证

1. 启动 `task-service`（消费者）
2. 启动 `order-service`（生产者）
3. 调接口发送：

```bash
GET http://localhost:808x/mq/send?msg=day9-test
```

4. 看 `task-service` 日志应出现：

```text
[task-service] 收到MQ消息: day9-test
```

## 7. 常见问题（你今天遇到的那类）

1. `消息看起来“没发出去”`：
- 实际上连接成功了，但没有消费者监听对应队列。

2. `消息被丢弃`：
- 队列名写错或队列不存在（默认交换机下会直接丢）。

3. `监听不到`：
- 两边不是同一个 `virtual-host`。
- `simple.queue` 和代码里的字符串不一致（大小写、点号都要一致）。

---

你先按这个最小链路改通。改通后下一步可以再加：
- `work queue`（多个消费者竞争）
- `fanout/direct/topic` 交换机路由
- 手动 ack、重试、死信队列
