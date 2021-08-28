package com.dobbinsoft.fw.support.annotation.cache;

import com.dobbinsoft.fw.core.Const;

import java.lang.annotation.*;

/**
 * 将对象组装起来的方式描述
 *
 * 分析：
 * 组装成什么对象，可以从方法签名中获取
 * KEY、Hash、都很好处理。
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAssemble {

    String key();

    String hashKey() default "";

    Class arrayClass() default Object.class;

    /**
     * 当缓存命中失败时
     * 是否更具读取描述，自动添加缓存
     * @return
     */
    boolean autoRefresh() default true;

    /**
     * 当缓存命中失败时 & 自动添加缓存为KV结构时
     * 自动添加的缓存保留周期 （秒）
     * @return
     */
    int expireSec() default Const.CACHE_ONE_DAY;

    String[] fields() default {};

    String[] excludeFields() default {};

}
