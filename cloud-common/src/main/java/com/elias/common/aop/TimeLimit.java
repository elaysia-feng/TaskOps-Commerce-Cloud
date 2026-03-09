package com.elias.common.aop;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 方法耗时阈值注解。
 *
 * 用法：
 * 1. 标在方法上：仅当前方法生效。
 * 2. 标在类上：类中全部方法默认生效，方法级注解可覆盖。
 */
//只是为了让这个注解出现在生成的 JavaDoc 文档里。
@Documented
//这个标签可以贴在方法上，也可以贴在类上。
@Target({ElementType.METHOD, ElementType.TYPE})
//这个标签在程序运行的时候依然存在
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeLimit {

    /**
     * 允许的最大耗时（毫秒）。
     */
    long value() default 500;

    /**
     * 是否在超时后抛异常。
     * false：只记录警告日志。
     * true：抛 BizException，中断请求。
     */
    boolean throwEx() default false;
}
