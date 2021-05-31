package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.mq.PulsarReliableMessageConfig;
import com.dobbinsoft.fw.support.config.open.OpenPlatformConfig;
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
@Import(PulsarReliableMessageConfig.class)
@Documented
public @interface EnablePulsarMQ {
}
