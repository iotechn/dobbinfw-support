package com.dobbinsoft.fw.core.annotation;

import com.dobbinsoft.fw.core.enums.BaseEnums;
import com.dobbinsoft.fw.core.enums.EmptyEnums;

import java.lang.annotation.*;

/**
 * Annotate on method parameters to define the parameter's passing method.
 * 注解在方法参数上，以定义该参数的传参方式
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpParam {

    // 参数名，前端传参名通常与方法的属性名相同
    // The parameter name; the name used for passing parameters from the frontend is usually the same as the attribute name of the method.
    String name();

    /**
     * 前端参数传入类型
     * COMMON: 普通参数
     * HEADER: 框架将会从Http请问的Header中读取
     * USER_ID: 当前登录用户ID 通常为Long类型，也许不会用于业务代码中
     * ADMIN_ID: 当前登录管理员ID 通常为Long类型，也许不会用于业务代码中
     * IP: 请求来源IP
     * FILE: 用户上传的文件 必须为 byte[]
     * EXCEL: 用户上传的Excel文件，会被解析为对应的导入对象数组（超大表会爆内存，适用于小表的导入）
     */
    /**
     * Frontend parameter input type
     * COMMON: Common parameter
     * HEADER: The framework will read it from the Header of the Http request
     * USER_ID: User usually uses the current logged-in user ID as a Long type, which may not be used in business logic.
     * ADMIN_ID: User usually uses the current logged-in administrator ID as a Long type, which may not be used in business logic.
     * IP: Request source IP
     * FILE: File uploaded by the user, must be byte[]
     * EXCEL: The Excel file uploaded by the user will be parsed into an array of corresponding import objects.
     */
    HttpParamType type() default HttpParamType.COMMON;

    // 对参数的描述
    // Description of the parameter
    String description() default "";

    // 参数默认值
    // Default value of the parameter
    String valueDef() default "";

    // 反序列化为数组时，其中的泛型（为了兼容Java的泛型擦出）
    // The generic type when deserializing into an array (for compatibility with Java's generic erasure).
    Class arrayClass() default Object.class;

    // 绑定枚举，用于生成文档
    // Binding enumeration for generating documentation
    Class<? extends BaseEnums> enums() default EmptyEnums.class;

    // 自定义账号类型
    Class customAccountClass() default Object.class;

}
