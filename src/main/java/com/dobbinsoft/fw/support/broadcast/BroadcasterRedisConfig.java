package com.dobbinsoft.fw.support.broadcast;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.mq.RedisExpiredListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class BroadcasterRedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                                   MessageListenerAdapter listenerAdapter,
                                                   RedisExpiredListener expiredListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(Const.BROADCAST_CHANNEL));
        container.addMessageListener(expiredListener, new PatternTopic("__keyevent@" + redisProperties.getDatabase() + "__:expired"));
        return container;
    }


    @Bean
    public MessageListenerAdapter listenerAdapter(BroadcastRedisReceiver subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisExpiredListener redisExpiredListener() {
        return new RedisExpiredListener();
    }

    @Bean
    public BroadcastRedisReceiver broadcastRedisReceiver() {
        return new BroadcastRedisReceiver();
    }

}
