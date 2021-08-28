package com.dobbinsoft.fw.support.annotation.cache;

public @interface CacheZSetJumpTo {

    String key();

    String hashKey() default "";

}
