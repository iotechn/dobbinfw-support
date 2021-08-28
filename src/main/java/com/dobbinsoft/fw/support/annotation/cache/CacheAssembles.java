package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 将多个封装方式组合起来
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAssembles {

    CacheAssemble[] value();

}
