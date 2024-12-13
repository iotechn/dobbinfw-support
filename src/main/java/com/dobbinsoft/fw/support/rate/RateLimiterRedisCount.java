package com.dobbinsoft.fw.support.rate;

import com.dobbinsoft.fw.core.annotation.HttpMethod;
import com.dobbinsoft.fw.core.annotation.RateLimitType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: RateLimiterRedisCount
 * Description: Redis实现计数器 限流器
 */
public class RateLimiterRedisCount implements RateLimiter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String COUNTER_BUCKET = "RT_COUNTER_";

    @Override
    public boolean acquire(String fullMethod, HttpMethod httpMethod, Long personId, String ip) {
        if (httpMethod.rateLimit() != RateLimitType.NONE) {
            String key = COUNTER_BUCKET + fullMethod;
            if (httpMethod.rateLimit() == RateLimitType.USER_ID) {
                key = key + "_U_" + personId;
            } else if (httpMethod.rateLimit() == RateLimitType.IP) {
                key = key + "_P_" + ip;
            }
            key = key + "_S_" + httpMethod.rateWindow();

            stringRedisTemplate.opsForValue().setIfAbsent(key, "0", httpMethod.rateWindow(), TimeUnit.SECONDS);
            Long increment = stringRedisTemplate.opsForValue().increment(key, 1l);
            // 计数器清0：当键值过期时，计数器清0
            // 计数器限流，存在临界问题
            // eg. 一个资源若允许60秒访问1000次。用户可以在00:59这一秒请求1000次，在01:00这一秒请求1000次，会导致在2秒内访问2000次，远超60秒1000次的设计。
            if (increment > httpMethod.rate()) {
                return false;
            }
        }
        return true;
    }

}
