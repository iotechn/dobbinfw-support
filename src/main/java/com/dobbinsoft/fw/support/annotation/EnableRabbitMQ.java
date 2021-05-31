package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.mq.RabbitReliableMessageConfig;
import com.dobbinsoft.fw.support.config.open.OpenPlatformConfig;
import com.dobbinsoft.fw.support.mq.RabbitReliableMessageQueue;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableOpenPlatform
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(RabbitReliableMessageConfig.class)
@Documented
public @interface EnableRabbitMQ {
}
