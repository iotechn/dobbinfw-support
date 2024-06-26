package com.dobbinsoft.fw.core.annotation.doc;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLog {

    // API变更日志,格式为 yyyy/MM/dd 变更内容包括字段增减/校验规则/类型修改等
    String[] value();

}
