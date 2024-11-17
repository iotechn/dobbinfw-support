package com.dobbinsoft.fw.support.mq;

import com.dobbinsoft.fw.support.component.CacheComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;

@Slf4j
public class RedisNotifyDelayedMessageQueueImpl implements DelayedMessageQueue {

    public static final String DELAY_TASK_ZSET = "DELAY_TASK_ZSET";

    @Autowired
    private CacheComponent cacheComponent;

    @Override
    public Boolean publishTask(Integer code, String value, Integer delay) {
        if (delay < 0) {
            delay = 1;
        }
        cacheComponent.putRaw(assembleKey(code, value), "", delay);
        cacheComponent.putZSet(assembleZSetKey(DELAY_TASK_ZSET), System.currentTimeMillis(), code + ":" + value);
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
        cacheComponent.putRaw(assembleKey(code, value), "", delay);
        cacheComponent.putZSet(assembleZSetKey(DELAY_TASK_ZSET), System.currentTimeMillis(), code + ":" + value);
        return true;
    }

    @Override
    public Boolean deleteTask(Integer code, String value) {
        cacheComponent.del(assembleKey(code, value));
        cacheComponent.delZSet(assembleZSetKey(DELAY_TASK_ZSET), code + ":" + value);
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
        StringBuilder sb = new StringBuilder("TASK:");
        sb.append(code);
        sb.append(":");
        sb.append(value);
        return sb.toString();
    }

    public String assembleZSetKey(String key) {
        return key;
    }

}
