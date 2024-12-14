package com.dobbinsoft.fw.support.config.mq;

import com.dobbinsoft.fw.support.mq.DelayedMessageHandler;
import com.dobbinsoft.fw.support.mq.DelayedMessageQueue;
import com.dobbinsoft.fw.support.mq.RedisExpiredListener;
import com.dobbinsoft.fw.support.mq.RedisNotifyDelayedMessageQueueImpl;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: DelayedMessageConfig
 * Description: Redis实现的延迟队列，原理是Redis键失效通知，需要打开此选项
 *
 */
public class DelayedMessageConfig {

    @Bean
    public Map<Integer, DelayedMessageHandler> messageHandleRouter(List<DelayedMessageHandler> delayedMessageHandlerList) {
        return delayedMessageHandlerList.stream().collect(Collectors.toMap(DelayedMessageHandler::getCode, v -> v));
    }

    @Bean
    public RedisExpiredListener redisExpiredListener() {
        return new RedisExpiredListener();
    }

    @Bean
    public DelayedMessageQueue delayedMessageQueue(){
        return new RedisNotifyDelayedMessageQueueImpl();
    }

}
