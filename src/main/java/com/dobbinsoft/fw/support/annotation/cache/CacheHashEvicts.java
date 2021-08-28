package com.dobbinsoft.fw.support.annotation.cache;


import java.lang.annotation.*;

/**
 * 将某个Hash的缓存无效化
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheHashEvicts {

    CacheHashEvict[] value();
}
