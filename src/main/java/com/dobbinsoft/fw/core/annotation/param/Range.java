package com.dobbinsoft.fw.core.annotation.param;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2018-08-20
 * Time: 上午11:11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface Range {

    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    String message() default "";

    boolean reqScope() default true;

    boolean respScope() default false;

}
