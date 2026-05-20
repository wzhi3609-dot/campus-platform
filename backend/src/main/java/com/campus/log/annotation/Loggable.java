package com.campus.log.annotation;

import java.lang.annotation.*;

/**
 * 自定义注解，标记需要记录操作日志的方法。
 * 通过 AOP 自动拦截并持久化操作记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    /**
     * 操作名称（如"审批通过用户"）。
     */
    String action() default "";

    /**
     * 操作目标类型（如"用户"、"帖子"）。
     */
    String target() default "";
}
