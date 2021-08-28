package com.dobbinsoft.fw.support.annotation.cache;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * 将缓存管理，交给框架去处理
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(rollbackFor = Exception.class)
@Documented
public @interface CacheProxy {
}
