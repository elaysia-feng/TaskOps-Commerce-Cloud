package com.elias.common.aop;

import com.elias.common.exception.BizException;
import com.elias.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * TimeLimit 注解切面。
 *
 * 作用：
 * 1. 拦截标注了 @TimeLimit 的方法（或类）。
 * 2. 方法执行后判断是否超出阈值。
 * 3. 超时时按配置选择：记录告警 或 抛出业务异常。
 */
@Slf4j
@Aspect
@Component
@Order(0)
public class TimeLimitAspect {

    /**
     * 环绕通知：处理方法级/类级 @TimeLimit。
     */
    @Around("@annotation(com.elias.common.aop.TimeLimit) || @within(com.elias.common.aop.TimeLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        TimeLimit limit = resolveTimeLimit(joinPoint);
        if (limit == null) {
            // 正常不会进入该分支，仅做防御性兜底。
            return result;
        }

        if (cost > limit.value()) {
            String method = joinPoint.getSignature().toShortString();
            String msg = String.format("方法执行超时: method=%s, cost=%dms, limit=%dms", method, cost, limit.value());
            if (limit.throwEx()) {
                // throwEx=true：直接抛错，交给全局异常处理。
                throw new BizException(ErrorCode.SERVER_ERROR, msg);
            }
            // throwEx=false：仅告警，不中断业务。
            log.warn("[AOP][TimeLimit] {}", msg);
        }
        return result;
    }

    /**
     * 解析生效的 TimeLimit：
     * 1. 优先方法级注解。
     * 2. 其次类级注解。
     */
    private TimeLimit resolveTimeLimit(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TimeLimit onMethod = method.getAnnotation(TimeLimit.class);
        if (onMethod != null) {
            return onMethod;
        }
        return joinPoint.getTarget().getClass().getAnnotation(TimeLimit.class);
    }
}
