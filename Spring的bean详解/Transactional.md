## Transactional

### 1. 事务失效出错

![局部截取_20260308_190341](E:\腾讯电脑管家截图文件\局部截取_20260308_190341.png)

这个事务失效了,因为在执行test方法的时候,是Userservice(并不是代理bean去)去调用的a方法,并不会去触发a方法上的事务功能,所以a方法的事务回滚出问题了

### 2. 解决方法

```java
public class UserService {
    private final OrderService orderService;

    // 构造函数注入
    public UserService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional
    public void test() { ... }
}
```

```java
public class OrderService {
    @Transactional(propagation = Propagation.NEVER)
    public void test() { ... }
}
```

![ChatGPT Image 2026年3月8日 19_26_13](C:\Users\seele\Downloads\ChatGPT Image 2026年3月8日 19_26_13.png)

![ChatGPT Image 2026年3月8日 19_26_36](C:\Users\seele\Downloads\ChatGPT Image 2026年3月8日 19_26_36.png)

### 3. bean工厂的对象创建时间

| Bean 类型     | AOP      | 创建时机                  | BeanFactory 中保存的对象 |
| ------------- | -------- | ------------------------- | ------------------------ |
| 单例普通 Bean | 无代理   | 启动时                    | 普通对象                 |
| 单例 AOP Bean | 代理对象 | 启动时                    | 代理对象                 |
| 懒加载 Bean   | √/ ×     | 第一次 `getBean()` 调用时 | 代理对象或普通对象       |
| 原型 Bean     | √/ ×     | 每次 `getBean()` 调用时   | 每次创建新实例           |