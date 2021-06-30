package com.dobbinsoft.fw.support.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2019-02-07
 * Time: 上午11:40
 */
@Component
public class LockComponent {

    @Autowired
    private StringRedisTemplate lockRedisTemplate;

    @Autowired
    private BeforeGetLockKey beforeGetLockKey;

    private static final String LOCK_PREFIX = "LOCK_PREFIX_";

    /**
     * 获取乐观锁
     *
     * @param key
     * @param timeoutSec 锁过期时间
     * @return
     */
    public boolean tryLock(String key, Integer timeoutSec) {
        return lockRedisTemplate.opsForValue().setIfAbsent(LOCK_PREFIX + getKey(key), System.currentTimeMillis() + "", Duration.ofSeconds(timeoutSec));
    }

    public boolean tryLockMulti(Collection<String> keys, Integer timeoutSec) {
        Map<String, String> map = new HashMap<>();
        String now = System.currentTimeMillis() + "";
        for (String key : keys) {
            map.put(getKey(key), now);
        }
        boolean suc = lockRedisTemplate.opsForValue().multiSetIfAbsent(map);
        if (suc) {
            keys.forEach(item -> {
                lockRedisTemplate.expire(item, timeoutSec, TimeUnit.SECONDS);
            });
        }
        return suc;
    }

    public void release(String key) {
        lockRedisTemplate.delete(LOCK_PREFIX + getKey(key));
    }

    public boolean hashPut(String table, String key) {
        return lockRedisTemplate.opsForHash().putIfAbsent(table, getKey(key), key);
    }

    public boolean hashContains(String table, String key) {
        return lockRedisTemplate.opsForHash().hasKey(table, getKey(key));
    }

    public void hashDel(String table, String key) {
        lockRedisTemplate.opsForHash().delete(table, getKey(key));
    }

    private String getKey(String key) {
        if (beforeGetLockKey != null) {
            return beforeGetLockKey.getKey(key);
        }
        return key;
    }
}
