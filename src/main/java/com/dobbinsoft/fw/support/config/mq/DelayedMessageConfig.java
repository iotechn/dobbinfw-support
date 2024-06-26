package com.dobbinsoft.fw.support.config.mq;

import com.dobbinsoft.fw.support.mq.DelayedMessageHandler;
import com.dobbinsoft.fw.support.mq.DelayedMessageQueue;
import com.dobbinsoft.fw.support.mq.RedisExpiredListener;
import com.dobbinsoft.fw.support.mq.RedisNotifyDelayedMessageQueueImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: DelayedMessageConfig
 * Description: Redis实现的延迟队列，原理是Redis键失效通知，需要打开此选项
 *
 */
public class DelayedMessageConfig {

    @Value("${spring.redis.database}")
    private Integer cacheDB;

    @Bean
    public Map<Integer, DelayedMessageHandler> messageHandleRouter(List<DelayedMessageHandler> delayedMessageHandlerList) {
        return delayedMessageHandlerList.stream().collect(Collectors.toMap(DelayedMessageHandler::getCode, v -> v));
    }

    @Bean
    public RedisExpiredListener redisExpiredListener() {
        return new RedisExpiredListener();
    }

    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory defaultLettuceConnectionFactory, RedisExpiredListener expiredListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(defaultLettuceConnectionFactory);
        container.addMessageListener(expiredListener, new PatternTopic("__keyevent@" + cacheDB + "__:expired"));
        return container;
    }

    @Bean
    public DelayedMessageQueue delayedMessageQueue(){
        return new RedisNotifyDelayedMessageQueueImpl();
    }

}
