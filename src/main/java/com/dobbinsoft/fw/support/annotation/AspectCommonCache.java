package com.dobbinsoft.fw.support.annotation;

import java.lang.annotation.*;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2020/3/28
 * Time: 13:59
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AspectCommonCache {

    /**
     * 键值
     * @return
     */
    String value() default "";

    int[] argIndex() default {};

    /**
     * <p>需要反序列化为List泛型才需要传入。否则会自动格式化为List《Map》是不影响</p>
     * @return <p>返回</p>
     */
    Class arrayClass() default Object.class;

    /**
     * <p>小于0代表不过期</p>
     * @return <p>返回</p>
     */
    int second() default 60 * 60 * 24;

}
