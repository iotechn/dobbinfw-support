package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 以KV结构存储缓存
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheKeyPuts {

    CacheKeyPut[] value();

}
