package com.dobbinsoft.fw.support.mq;

import com.dobbinsoft.fw.support.component.CacheComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
public class RedisNotifyDelayedMessageQueueImpl implements DelayedMessageQueue {

    private static final String DELAYED_MQ_HASH_TAG = "{delayed_mq}";

    public static final String DELAYED_TASK_ZSET = "IGNORE_TENEMENT:%s_DELAY_TASK_ZSET".formatted(DELAYED_MQ_HASH_TAG);
    public static final String DELAYED_TASK_TASK_PREFIX = "%s_TASK".formatted(DELAYED_MQ_HASH_TAG);

    @Autowired
    private CacheComponent cacheComponent;

    private volatile boolean isFirstTime = true;

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        if (isFirstTime) {
            isFirstTime = false;
            // 将此刻以前的任务全部重新丢会延迟队列，并设置为1s后执行
            Set<String> retryKeyValues = cacheComponent.getZSetScoreLessThan(assembleZSetKey(DELAYED_TASK_ZSET), System.currentTimeMillis());
            for (String retryKeyValue : retryKeyValues) {
                cacheComponent.putRawAndZSetNoProcess(retryKeyValue, assembleZSetKey(DELAYED_TASK_ZSET), System.currentTimeMillis(), retryKeyValue, 1);
                log.info("[delayed mq] 服务重启补偿Key={}", retryKeyValue);
            }
        }
    }

    @Override
    public Boolean publishTask(Integer code, String value, Integer delay) {
        if (delay < 0) {
            delay = 1;
        }
        String keyValue = assembleKey(code, value);
        // 使用lua同时写入KV 和 ZSet
        cacheComponent.putRawAndZSet(keyValue, assembleZSetKey(DELAYED_TASK_ZSET), System.currentTimeMillis() + (1000L * delay), cacheComponent.getKey(keyValue), delay);
        return true;
    }

    @Override
    public Boolean publishTaskAt(Integer code, String value, LocalDateTime executeTime) {
        LocalDateTime now = LocalDateTime.now();
        int delay;
        if (executeTime.isBefore(now)) {
            delay = 1;
        } else {
            Duration duration = Duration.between(now, executeTime);
            delay = (int) duration.getSeconds();
        }
        String keyValue = assembleKey(code, value);
        cacheComponent.putRawAndZSet(keyValue, assembleZSetKey(DELAYED_TASK_ZSET), System.currentTimeMillis() + (1000L * delay), cacheComponent.getKey(keyValue), delay);
        return true;
    }

    @Override
    public Boolean deleteTask(Integer code, String value) {
        String keyValue = assembleKey(code, value);
        cacheComponent.del(keyValue);
        cacheComponent.delZSet(assembleZSetKey(DELAYED_TASK_ZSET), cacheComponent.getKey(keyValue));
        return true;
    }

    @Override
    public Long getTaskTime(Integer code, String value) {
        return cacheComponent.getKeyExpire(assembleKey(code, value));
    }

    public String assembleKey(Integer code, String value) {
        if (value == null) {
            value = "";
        }
        StringBuilder sb = new StringBuilder(DELAYED_TASK_TASK_PREFIX);
        sb.append(":");
        sb.append(code);
        sb.append(":");
        sb.append(value);
        return sb.toString();
    }

    public String assembleZSetKey(String key) {
        return key;
    }

}
