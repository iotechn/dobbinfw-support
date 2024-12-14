package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.broadcast.BroadcasterRedisConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(BroadcasterRedisConfig.class)
@Documented
public @interface EnableBroadcast {
}
