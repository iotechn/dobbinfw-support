package com.dobbinsoft.fw.core.annotation;

import com.dobbinsoft.fw.support.config.db.cache.CacheKeyConditionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在Domain上，表示所有的查询，写入一定会携带此字段，刷新缓存时也将局部刷新。
 * 例如购物车，查询一定与用户相关，写入 userId
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheKeyCondition {

    public String key();

    /**
     *
     * 框架提供实现
     * CacheKeyConditionProvider.UserId.class
     *
     * 进阶用法
     * 自己实现 CacheKeyConditionProvider， 并放入IOC
     * @return
     */
    public Class<? extends CacheKeyConditionProvider> conditionProvider();

}
