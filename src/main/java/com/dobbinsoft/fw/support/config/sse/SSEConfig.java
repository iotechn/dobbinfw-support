package com.dobbinsoft.fw.support.config.sse;

import com.dobbinsoft.fw.support.broadcast.Broadcaster;
import com.dobbinsoft.fw.support.sse.SSEBroadcastShareListener;
import com.dobbinsoft.fw.support.sse.SSEPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

public class SSEConfig {

    @Bean
    @ConditionalOnBean(Broadcaster.class)
    public SSEBroadcastShareListener sseBroadcastShareListener() {
        return new SSEBroadcastShareListener();
    }

    @Bean
    public SSEPublisher ssePublisher() {
        return new SSEPublisher();
    }

}
