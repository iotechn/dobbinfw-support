package com.dobbinsoft.fw.support.config.broadcast;

import com.dobbinsoft.fw.support.broadcast.BroadcastRedisReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

public class BroadcasterRedisConfig {

    @Bean
    public MessageListenerAdapter listenerAdapter(BroadcastRedisReceiver subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public BroadcastRedisReceiver broadcastRedisReceiver() {
        return new BroadcastRedisReceiver();
    }

}
