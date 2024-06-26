package com.dobbinsoft.fw.core.annotation.param;

import java.lang.annotation.*;

// 注解在任意参数与对象字段上，用于校验该字段不能为空，如果是在注解在包含HttpParam注解的参数上，您可以断定此对象非空，并不需要在业务代码中做二次参数校验
// The annotation is placed on any parameter or object field to validate that the field is not empty. If the annotation is placed on a parameter that already contains the HttpParam annotation, you can assume that the object is not empty and do not need to perform secondary parameter validation in the business code.
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface NotNull {
    // 如果字段为空需要提示给前端的信息，通常使用参数中文描述 + “不能为空”的描述，例如“用户手机号不能为空”
    // If the field is empty, the information to be displayed to the frontend is usually the Chinese parameter description followed by "不能为空" (cannot be empty), for example, "用户手机号不能为空" (User phone number cannot be empty).
    String message() default "";
}
