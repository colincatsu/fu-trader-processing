package com.future.trader.common.annotation;

import org.springframework.core.MethodParameter;

import java.lang.annotation.*;

/**
 * 放行使用此注解的controller方法，即不执行统一响应处理{@link }
 *
 * @author Admin
 * @version: 1.0
 * @see ( MethodParameter , Class )
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseExclusion {
}
