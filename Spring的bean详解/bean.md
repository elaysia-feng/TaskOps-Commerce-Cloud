# Spring Bean 详解: 为什么“不注入”通常意味着耦合更高

我在这个目录下放了一个可以直接运行的小例子:

- `bean-demo/`：完整 Maven 示例
- `bean-demo/src/main/java/com/elias/beanexample/`：核心代码

## 1. 先看结论

很多人会把“没有用 Spring 注入”和“耦合高”直接画等号，但更准确的说法是：

- 真正导致高耦合的，不是“没用 Spring”本身。
- 真正导致高耦合的是：`业务类自己决定依赖的具体实现，并且自己负责 new 出来。`
- Spring Bean 注入的价值，是把“对象创建”和“对象使用”拆开。

也就是说：

- `new` 在业务类内部：高耦合
- 构造器传参手动注入：已经在解耦
- Spring Bean 注入：在“手动注入”的基础上，把组装过程交给容器统一管理

## 2. 这个例子演示了三种写法

### 写法一：类内部自己 `new`

对应类：

- `OrderServiceHardCoupled`

核心代码：

```java
private final MySqlOrderRepository orderRepository = new MySqlOrderRepository();
private final SmsMessageSender messageSender = new SmsMessageSender();
```

这会带来 4 个直接问题：

1. `OrderServiceHardCoupled` 直接依赖具体类，而不是依赖抽象。
2. 想把短信改成邮件，必须改服务类源码。
3. 想做测试替身（mock/fake），很难替换。
4. 对象创建责任和业务责任混在一起。

所以它“耦合高”的本质是：

`OrderServiceHardCoupled` 不只是“用依赖”，它还“绑死了依赖”。

## 3. 写法二：手动依赖注入

对应类：

- `OrderServiceManualInjected`

核心代码：

```java
public OrderServiceManualInjected(OrderRepository orderRepository, MessageSender messageSender) {
    this.orderRepository = orderRepository;
    this.messageSender = messageSender;
}
```

这时候服务类只关心两件事：

- 要有一个 `OrderRepository`
- 要有一个 `MessageSender`

至于到底是：

- `MySqlOrderRepository`
- `InMemoryOrderRepository`
- `SmsMessageSender`
- `EmailMessageSender`

都由外部决定。

这说明一个关键点：

`依赖注入` 这件事，Spring 不是发明者，Spring 只是把它做成了容器能力。

## 4. 写法三：Spring Bean 注入

对应类：

- `OrderServiceSpringInjected`
- `BeanDemoConfig`
- `AlternativeBeanConfig`

这里业务类仍然只依赖接口，区别只是：

- 以前你自己 `new OrderServiceManualInjected(...)`
- 现在由 Spring 容器调用 `@Bean` 方法来组装对象

例如：

```java
@Bean
public OrderServiceSpringInjected orderServiceSpringInjected(OrderRepository orderRepository,
                                                             MessageSender messageSender) {
    return new OrderServiceSpringInjected(orderRepository, messageSender);
}
```

Spring 会做两件事：

1. 先找到或创建 `OrderRepository` bean
2. 再找到或创建 `MessageSender` bean
3. 最后把它们注入到 `OrderServiceSpringInjected`

## 5. 为什么说 Spring 注入能降低耦合

因为它把一个类对外部的依赖关系，从：

`我自己决定依赖谁 + 我自己创建它`

变成了：

`我只声明我需要什么，至于给我谁，由外部容器决定`

这样带来的直接好处：

1. 业务类依赖接口，不依赖具体实现。
2. 替换实现时，大多数情况只改配置或装配代码。
3. 测试时更容易塞入 fake/mock。
4. 生命周期统一管理，比如单例、多例、懒加载、初始化、销毁。
5. 更适合大型项目，避免到处 `new` 导致对象关系失控。

## 6. 你最应该抓住的一句话

高耦合不是因为“没写 `@Autowired`”。

高耦合是因为：

`类把“依赖抽象什么”和“依赖具体谁”这两件事同时写死了。`

而 Spring Bean 注入，是把“依赖具体谁”的决定权，移出了业务类。

## 7. 怎么运行这个例子

进入目录：

```powershell
cd E:\Java\springBoot\Spring的bean详解\bean-demo
```

执行：

```powershell
mvn compile exec:java
```

你会看到 4 段输出：

1. 高耦合写法
2. 手动注入写法
3. Spring 注入写法
4. 只改配置就替换依赖的写法

## 8. 建议你重点对比的文件

- `bean-demo/src/main/java/com/elias/beanexample/OrderServiceHardCoupled.java`
- `bean-demo/src/main/java/com/elias/beanexample/OrderServiceManualInjected.java`
- `bean-demo/src/main/java/com/elias/beanexample/OrderServiceSpringInjected.java`
- `bean-demo/src/main/java/com/elias/beanexample/BeanDemoConfig.java`
- `bean-demo/src/main/java/com/elias/beanexample/AlternativeBeanConfig.java`

## 9. 一句话区分三个概念

- `Bean`：被 Spring 容器管理的对象
- `IOC`：对象不再由业务类自己创建，而是交给容器管理
- `DI`：容器把对象依赖注入给你

如果你愿意，我下一步可以继续在这个目录里再补一份：

- `@Component + @Autowired` 版本
- `@Resource` 和 `@Autowired` 的区别
- 单例 bean / 多例 bean 的区别
- 循环依赖为什么会出问题
