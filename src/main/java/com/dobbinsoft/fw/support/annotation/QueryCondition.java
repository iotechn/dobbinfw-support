package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.annotation.enums.Conditions;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryCondition {

    public String field() default "";

    public Conditions condition() default Conditions.EQUAL;

}
