package com.dobbinsoft.fw.support.rate;

import com.dobbinsoft.fw.core.annotation.HttpMethod;
import com.dobbinsoft.fw.core.annotation.RateLimitType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RateLimiterRedisSlidingWindow
 * Description: Redis 实现 滑动窗口 限流器
 *
 */
@Slf4j
public class RateLimiterRedisSlidingWindow implements RateLimiter {

    @Autowired
    private StringRedisTemplate lockRedisTemplate;

    private static final String SLIDING_BUCKET = "RT_SLIDING_";



    /**
     * 将窗口分为6格 分别为Hash表的 K0 --- K5
     */
    private static final int DIV = 6;

    /**
     * 时间戳KEY
     */
    private static final String TIMESTAMP_KEY = "TIMESTAMP_KEY";

    /**
     * IO 操作 不滑动窗口 2
     *         滑动窗口  4
     * @param fullMethod 方法全名 group.method
     * @param httpMethod 方法注解
     * @param personId 人的ID，管理员 或 用户
     * @param ip 外网IP
     * @return
     */
    @Override
    public boolean acquire(String fullMethod, HttpMethod httpMethod, Long personId, String ip) {
        if (httpMethod.rateLimit() != RateLimitType.NONE) {
            String key = SLIDING_BUCKET + fullMethod;
            if (httpMethod.rateLimit() == RateLimitType.USER_ID) {
                key = key + "_U_" + personId;
            } else if (httpMethod.rateLimit() == RateLimitType.IP) {
                key = key + "_P_" + ip;
            }
            key = key + "_S_" + httpMethod.rateWindow();
            // 大窗 ms
            int bigWindow = httpMethod.rateWindow() * 1000;
            // 小窗
            int smallWindow = bigWindow / DIV;
            TimeHolder timeHolder = new TimeHolder();
            Map<Object, Object> entries = lockRedisTemplate.opsForHash().entries(key);
            int sum = entries.values().stream().filter(item -> {
                long v = new Long((String) item);
                if (v > 1618284149838L) {
                    // 为了减少一次redis操作，hash表里面会存一个时间戳，大于这个阈值的就认为是时间戳
                    // 也可以通过KEY来，但是会在JAVA增加多次Hash操作，不划算
                    timeHolder.setTimestamp(v);
                    return false;
                } else {
                    return true;
                }
            }).mapToInt(item -> new Integer((String) item)).sum();

            // 系统时间
            long nowTime = System.currentTimeMillis();
            // 开窗时间
            long openTime = timeHolder.getTimestamp();

            long indexRaw = (nowTime - openTime) / smallWindow;
            int index;
            if (indexRaw - (DIV - 1) > DIV) {
                // 全部清空
                lockRedisTemplate.delete(key);
                lockRedisTemplate.opsForHash().put(key, TIMESTAMP_KEY, nowTime + "");
                lockRedisTemplate.expire(key, 12, TimeUnit.HOURS);
                sum = 0;
                index = 0;
            } else if (indexRaw > DIV - 1){
                int delta = (int)(indexRaw - (DIV - 1));
                // 将大窗滑动 & 并减去前 delta 个小窗的值
                for (int i = 0; i < DIV; i++) {
                    if (i < delta) {
                        String num = (String)entries.get("K" + i);
                        if (num != null) {
                            sum -= Integer.parseInt(num);
                        }
                    }
                    if (i + delta > DIV) {
                        entries.put("K" + i, "0");
                    } else {
                        Object o = entries.get("K" + (i + delta));
                        entries.put("K" + i, StringUtils.isEmpty(o) ? "0" : o);
                    }
                }
                entries.put(TIMESTAMP_KEY, nowTime + "");
                lockRedisTemplate.opsForHash().putAll(key, entries);
                index = DIV - 1;
            } else {
                index = (int) indexRaw;
            }

            if (sum >= httpMethod.rate()) {
                return false;
            }
            lockRedisTemplate.opsForHash().increment(key, "K" + index, 1l);

        }
        return true;
    }

    @Data
    private static class TimeHolder {

        /**
         * 默认开窗时间为0，也不会影响计数
         */
        private Long timestamp = 0L;

    }

}
