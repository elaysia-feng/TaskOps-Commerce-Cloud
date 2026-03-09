## Common阅读

### 1. Perforaop

#### 1.1 Around : 相当于 before和after的加强版

```md
如果你在一个切面里同时写了这俩，Spring 会像剥洋葱一样执行。顺序如下：

Around 的前半部分（pjp.proceed() 之前的代码）

Before

真正的业务方法

Around 的后半部分（pjp.proceed() 之后的代码）
```

```java
package com.example.demo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect    // 声明这是一个切面
@Component // 交给 Spring 管理
@Slf4j
public class LogAspect {

    // 1. 定义切点：监控 com.example.demo.service 包下所有类的所有方法
    @Pointcut("execution(* com.example.demo.service..*(..))")
    public void serviceMethods() {}

    // 2. 环绕通知：控制权最强，负责计时
    @Around("serviceMethods()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = pjp.getSignature().getName();
        
        try {
            // 【关键点】手动触发业务方法执行
            Object result = pjp.proceed(); 
            
            long cost = System.currentTimeMillis() - start;
            log.info("[AOP 环绕] 方法 {} 执行成功，耗时 {}ms", methodName, cost);
            return result; // 返回业务结果
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.error("[AOP 环绕] 方法 {} 执行失败，耗时 {}ms", methodName, cost);
            throw e; // 继续抛出，让 @AfterThrowing 也能抓到
        }
    }

    // 3. 前置通知：执行前打个招呼
    @Before("serviceMethods()")
    public void before(JoinPoint jp) {
        Object[] args = jp.getArgs(); // 获取方法参数
        log.info("[AOP 前置] 准备执行方法: {}, 参数: {}", jp.getSignature().getName(), args[0]);
    }

    // 4. 异常通知：只有出事了才执行
    @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
    public void afterThrowing(JoinPoint jp, Throwable ex) {
        log.error("[AOP 异常] 方法 {} 抛出异常: {}", jp.getSignature().getName(), ex.getMessage());
    }
}
```

#### 1.2 这个切面干了啥

`就是执行方法的时间`

### 2.TimeLimitAspect

#### 2.1 @Order(0)

这个代表执行顺序,0是最高的优先级

#### 2.2 @Target({ElementType.METHOD, ElementType.TYPE})

这个是声名注解的时候用的

`这个标签可以贴在方法上，也可以贴在类上。`

#### 2.3 @Retention(RetentionPolicy.RUNTIME)

这个也是声名注解的时候用的

`@Retention(RetentionPolicy.RUNTIME)`

#### 2.4 声名注解的方式

`@interface`

例:

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeLimit {
    long value() default 500;
    boolean throwEx() default false;
}

```

### 3.context

#### 3.1 UserContext

它是一个**基于 `ThreadLocal` 的轻量级全局“用户身份证”存储器**，让你能在业务代码的任何角落（哪怕是底层）随时获取当前登录用户的信息，而无需到处传递参数

#### 3.2 Filter

在每个`http`请求来的时候都执行一遍,把http里面的token里面的`userId, username, role`角色都放进去

