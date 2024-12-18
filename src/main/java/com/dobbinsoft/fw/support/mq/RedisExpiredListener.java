package com.dobbinsoft.fw.support.mq;

import com.dobbinsoft.fw.support.component.CacheComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.HashMap;

public class RedisExpiredListener implements MessageListener, ApplicationContextAware {

    /**
     * 客户端监听订阅的topic，当有消息的时候，会触发该方法;
     * 并不能得到value, 只能得到key。
     * 姑且理解为: redis服务在key失效时(或失效后)通知到java服务某个key失效了, 那么在java中不可能得到这个redis-key对应的redis-value。
     */
    protected HashMap<Integer, DelayedMessageHandler> handlerRouter;

    protected RedisNotifyDelayedMessageQueueImpl queue;

    @Autowired(required = false)
    private DelayedMessageRedisKeyParser delayedMessageRedisKeyParser;

    @Autowired
    private CacheComponent cacheComponent;

    private static final Logger logger = LoggerFactory.getLogger(RedisExpiredListener.class);

    @Override
    public void onMessage(Message message, byte[] bytes) {
        // 后续删除要用
        final String expiredKey = message.toString();
        // TASK:CODE:VALUE结构
        String[] split;
        if (delayedMessageRedisKeyParser != null) {
            split = delayedMessageRedisKeyParser.parse(expiredKey);
        } else {
            split = expiredKey.split(":");
        }
        if (split.length < 2 || !split[0].equals("TASK")) {
            return;
        }
        logger.info("[Redis键失效通知] key=" + expiredKey);
        StringBuilder value = new StringBuilder();
        for (int i = 2; i < split.length; i++) {
            value.append(split[i]);
            if (i != split.length - 1) {
                value.append(":");
            }
        }
        int code = Integer.parseInt(split[1]);
        DelayedMessageHandler handler = handlerRouter.get(code);
        if (handler != null) {
            handler.handle(value.toString());
        }

        // 将value从ZSet删除
        String zSetKey = queue.assembleZSetKey(RedisNotifyDelayedMessageQueueImpl.DELAYED_TASK_ZSET);
        // 滑动指针，并对未处理的消息，进行重新处理
        cacheComponent.delZSet(zSetKey, expiredKey);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.handlerRouter = (HashMap<Integer, DelayedMessageHandler>) applicationContext.getBean("messageHandleRouter");
        this.queue = applicationContext.getBean(RedisNotifyDelayedMessageQueueImpl.class);
    }
}