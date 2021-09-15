package com.dobbinsoft.fw.support.config.mq;

import com.dobbinsoft.fw.support.mq.DelayedMessageHandler;
import com.dobbinsoft.fw.support.mq.DelayedMessageQueue;
import com.dobbinsoft.fw.support.mq.RedisExpiredListener;
import com.dobbinsoft.fw.support.mq.RedisNotifyDelayedMessageQueueImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName: DelayedMessageConfig
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-03-18
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
