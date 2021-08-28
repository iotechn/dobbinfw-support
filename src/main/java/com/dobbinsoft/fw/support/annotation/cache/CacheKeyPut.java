package com.dobbinsoft.fw.support.annotation.cache;

import com.dobbinsoft.fw.core.Const;

import java.lang.annotation.*;

/**
 * 以KV结构存储缓存
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheKeyPut {

    String key();

    String value();

    int expireSec() default Const.CACHE_ONE_DAY;

    String[] fields() default {};

    String[] excludeFields() default {};

}
