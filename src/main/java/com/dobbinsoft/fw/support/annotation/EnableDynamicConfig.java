package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.dynamic.DynamicConfig;
import com.dobbinsoft.fw.support.config.mq.RabbitReliableMessageConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableDynamicConfig
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(DynamicConfig.class)
@Documented
public @interface EnableDynamicConfig {
}
