package com.dobbinsoft.fw.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在一个Service接口的方法上，标记方法需要暴露为WebAPI
 * Annotate a method in a service interface to mark it as a method that needs to be exposed as a Web API.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpMethod {
    // 对API功能进行简单描述
    // Provide a brief description of the API functionality.
    String description();
    // API限流类型(NONE:不限流, USER_ID:对用户访问频率限流, IP:对来源IP限流, ALL:对全局访问限流)
    // API rate limiting type (NONE: no rate limiting, USER_ID: rate limiting based on user access frequency, IP: rate limiting based on source IP, ALL: global rate limiting)
    RateLimitType rateLimit() default RateLimitType.NONE;
    // API限流时间窗口(单位秒)
    // API rate limiting time window (in seconds)
    int rateWindow() default 60;
    // API限流频率，例如rateLimit=RateLimitType.IP,rateWindow=60,rate=1,意思是任意IP只能在1分钟内访问1次接口
    // API rate limiting frequency, for example, rateLimit=RateLimitType.IP, rateWindow=60, rate=1, means that any IP can only access the API once within 1 minute.
    int rate() default 1;
    String permission() default "";
    String permissionParentName() default "";
    String permissionName() default "";
    String contentType() default "";
    String exportFileName() default "";
}
