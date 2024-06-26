package com.dobbinsoft.fw.core.annotation.param;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2018-08-20
 * Time: 上午10:51
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface TextFormat {

    String regex() default "";

    String[] contains() default {};

    String[] notContains() default {};

    String startWith() default "";

    String endsWith() default "";

    int lengthMax() default Integer.MAX_VALUE;

    int lengthMin() default 0;

    int length() default -1;

    boolean notChinese() default false;

    String message() default "";

    boolean reqScope() default true;

    boolean respScope() default false;

}
