package com.dobbinsoft.fw.support.broadcast;

import com.dobbinsoft.fw.core.Const;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class BroadcasterRedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory defaultLettuceConnectionFactory,
                                                                       MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(defaultLettuceConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(Const.BROADCAST_CHANNEL));
        return container;
    }


    @Bean
    public MessageListenerAdapter listenerAdapter(BroadcastRedisReceiver subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public BroadcastRedisReceiver broadcastRedisReceiver() {
        return new BroadcastRedisReceiver();
    }

}
