package com.dobbinsoft.fw.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CN：注解在一个Service接口上，标记接口是需要被暴露为WebAPI的接口
 * EN：Annotate a service interface to mark it as an interface that needs to be exposed as a Web API.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpOpenApi {
    // API所在分组，例如UserService可传入user
    // The group in which the API is located, for example, UserService can be passed in as "user."
    String group();
    // API分组描述，通常用于中文描述。例如UserService可传入用户服务
    // The group description of the API, typically used for Chinese description. For example, UserService can be described as "用户服务" (User Service).
    String description();
}
