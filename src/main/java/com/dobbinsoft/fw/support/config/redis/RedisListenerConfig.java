package com.dobbinsoft.fw.support.config.redis;

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
public class RedisListenerConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Autowired(required = false)
    private MessageListenerAdapter messageListenerAdapter;

    @Autowired(required = false)
    private RedisExpiredListener redisExpiredListener;

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory) {
        if (messageListenerAdapter == null && redisExpiredListener == null) {
            return null;
        }
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        if (messageListenerAdapter != null) {
            container.addMessageListener(messageListenerAdapter, new PatternTopic(Const.BROADCAST_CHANNEL));
        }
        if (redisExpiredListener != null) {
            container.addMessageListener(redisExpiredListener, new PatternTopic("__keyevent@" + redisProperties.getDatabase() + "__:expired"));
        }
        return container;
    }

}
