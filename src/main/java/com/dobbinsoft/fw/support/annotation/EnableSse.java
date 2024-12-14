package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.sse.SSEConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(SSEConfig.class)
@Documented
public @interface EnableSse {

    // 如果在集群模式下，需要借助redis广播将消息路由到不同的节点上。需要同时开启
    // @EnableBroadcast

}
