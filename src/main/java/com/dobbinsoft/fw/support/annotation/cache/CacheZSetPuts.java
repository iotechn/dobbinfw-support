package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 将value设置到
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheZSetPuts {

    CacheZSetPut[] value();

}
