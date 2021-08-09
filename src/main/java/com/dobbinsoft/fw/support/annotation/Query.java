package com.dobbinsoft.fw.support.annotation;

import java.lang.annotation.*;

/**
 * 查表注解，使用此注解，标记接口是一个查询接口。
 * 框架可为其封装一个条件构造器 QueryWrapper
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Query {

    /**
     * 结果集是否升序
     * @return
     */
    public boolean isAsc() default true;

    /**
     * 默认主键升序
     * @return
     */
    public String[] sort() default {"id"};

    /**
     * 选择哪些字段
     * @return
     */
    public String[] select() default {};

}
