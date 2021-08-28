package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 将入参的部分（全部）属性形成对象，放入Hash桶里面
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheHashPut {

    String key();

    String hashKey();

    String value();

    String[] fields() default {};

    String[] excludeFields() default {};

}
