package com.dobbinsoft.fw.support.mq;

import com.dobbinsoft.fw.support.component.CacheComponent;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Set;

/**
 * 延迟消息可靠性保障
 * 若仅使用键失效通知，如果出现网络中断，或应用服务器正在重启，会导致redis的消息丢失。
 * 所以增加一个机制，在ZSet中冗余一份要执行的Task，并且处理后删除冗余数据。增加定时任务，每10秒扫描一次，是否有执行漏掉的任务
 *
 * 开销： 每10秒查询一次redis
 */
@Slf4j
@EnableScheduling
public class RedisDelayedQuartz implements ApplicationContextAware {

    @Autowired
    private RedisNotifyDelayedMessageQueueImpl redisNotifyDelayedMessageQueue;


    @Autowired
    private CacheComponent cacheComponent;

    /**
     * 客户端监听订阅的topic，当有消息的时候，会触发该方法;
     * 并不能得到value, 只能得到key。
     * 姑且理解为: redis服务在key失效时(或失效后)通知到java服务某个key失效了, 那么在java中不可能得到这个redis-key对应的redis-value。
     */
    protected HashMap<Integer, DelayedMessageHandler> handlerRouter;


    @Scheduled(cron = "0/10 * * * * ? ")
    public void check() {
        String zSetKey = redisNotifyDelayedMessageQueue.assembleZSetKey(RedisNotifyDelayedMessageQueueImpl.DELAY_TASK_ZSET);

        Set<ZSetOperations.TypedTuple<String>> range = cacheComponent.getZSetTopNWithScore(zSetKey, 10);
        if (CollectionUtils.isNotEmpty(range)) {
            for (ZSetOperations.TypedTuple<String> expiredKeyTuple : range) {
                String expiredKey = expiredKeyTuple.getValue();
                Double score = expiredKeyTuple.getScore();
                assert score != null;
                // 防止处理时间过长，而触发补偿，如果处理时间超过2秒，就只能通过锁去解决了。
                if (score.longValue() < System.currentTimeMillis() - 2 * 1000L) {
                    // CODE:VALUE结构
                    assert expiredKey != null;
                    String[] split = expiredKey.split(":");
                    if (split.length < 2) {
                        return;
                    }
                    log.info("[Redis消息补偿通知] key=" + expiredKey);
                    StringBuilder value = new StringBuilder();
                    for (int i = 1; i < split.length; i++) {
                        value.append(split[i]);
                        if (i != split.length - 1) {
                            value.append(":");
                        }
                    }
                    int code = Integer.parseInt(split[0]);
                    DelayedMessageHandler handler = handlerRouter.get(code);
                    if (handler != null) {
                        handler.handle(value.toString());
                    }
                    // 将value从ZSet删除
                    cacheComponent.delZSet(zSetKey, expiredKey);
                }

            }
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.handlerRouter = (HashMap<Integer, DelayedMessageHandler>) applicationContext.getBean("messageHandleRouter");
    }

}
