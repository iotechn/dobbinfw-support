package com.dobbinsoft.fw.support.annotation.cache;

import java.lang.annotation.*;

/**
 * 组装成序列
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAssembleArray {

    /**
     * page limit 两个字段非空 表示分页查询
     * @return
     */
    String page() default "";

    String limit() default "";

    /**
     * 默认的序列
     * @return
     */
    String key();

    /**
     * 排序字段
     * @return
     */
    String sort() default "";

    /**
     * 排序映射
     * CACHE_KEY|sortFiled
     * @return
     */
    String[] sortMapping() default {};

    /**
     * 是否升序，默认升序
     * @return
     */
    String isAsc() default "";

    /**
     * 跳转到哪个 Hash 桶
     * @return
     */
    String jump();

    Class arrayClass();

}
