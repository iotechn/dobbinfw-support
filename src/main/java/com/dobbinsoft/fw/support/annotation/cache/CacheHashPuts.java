package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 组合封装Hash
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheHashPuts {

    CacheHashPut[] value();

}
