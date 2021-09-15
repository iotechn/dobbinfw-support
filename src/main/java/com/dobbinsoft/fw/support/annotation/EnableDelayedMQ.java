package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.mq.DelayedMessageConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableDelayMQ
 * Description: 延迟队列
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(DelayedMessageConfig.class)
@Documented
public @interface EnableDelayedMQ {
}
