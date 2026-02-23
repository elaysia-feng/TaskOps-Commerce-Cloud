package com.elias.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 通用性能日志切面。
 *
 * 设计目标：
 * 1. 统一记录 controller/service 方法的执行耗时。
 * 2. 异常时也记录耗时，便于快速定位慢方法和失败路径。
 * 3. 避免打印敏感参数值，仅输出参数类型摘要。
 */
@Slf4j
@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class PerformanceLogAspect {

    /**
     * controller 层切点：匹配 com.elias..controller.. 包下全部方法。
     */
    @Pointcut("execution(* com.elias..controller..*(..))")
    public void controllerMethods() {
        // 切点方法体留空，仅用于命名和复用。
    }

    /**
     * service 层切点：匹配 com.elias..service.. 包下全部方法。
     */
    @Pointcut("execution(* com.elias..service..*(..))")
    public void serviceMethods() {
        // 切点方法体留空，仅用于命名和复用。
    }

    /**
     * 环绕通知：在目标方法前后统计耗时并输出日志。
     *
     * 逻辑：
     * 1. 方法执行前开始计时。
     * 2. 执行目标方法。
     * 3. 成功时记录 success 日志。
     * 4. 失败时记录 failed 日志并原样抛出异常。
     */
    @Around("controllerMethods() || serviceMethods()")
    public Object logCost(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        String signature = joinPoint.getSignature().toShortString();
        String argSummary = summarizeArgs(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();
            log.info("[AOP] success method={}, cost={}ms, args={}",
                    signature, stopWatch.getTotalTimeMillis(), argSummary);
            return result;
        } catch (Throwable ex) {
            stopWatch.stop();
            log.error("[AOP] failed method={}, cost={}ms, args={}, ex={}",
                    signature, stopWatch.getTotalTimeMillis(), argSummary, ex.toString());
            throw ex;
        }
    }

    /**
     * 将参数转换为安全摘要：只保留类型信息，不输出具体值。
     */
    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .map(this::safeArgType)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * 参数安全描述：
     * - null -> "null"
     * - LoginRequest/RegisterRequest -> Xxx(masked)
     * - 其他 -> 类名
     */
    private String safeArgType(Object arg) {
        if (arg == null) {
            return "null";
        }
        String className = arg.getClass().getSimpleName();
        if ("LoginRequest".equals(className) || "RegisterRequest".equals(className)) {
            return className + "(masked)";
        }
        return className;
    }
}
