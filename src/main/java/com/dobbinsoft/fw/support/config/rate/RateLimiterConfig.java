package com.dobbinsoft.fw.support.config.rate;

import com.dobbinsoft.fw.support.properties.FwRateLimitProperties;
import com.dobbinsoft.fw.support.rate.RateLimiterRedisSlidingWindow;
import com.dobbinsoft.fw.support.rate.RateLimiter;
import com.dobbinsoft.fw.support.rate.RateLimiterNone;
import com.dobbinsoft.fw.support.rate.RateLimiterRedisCount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RateLimiterConfig
 * Description: 接口级别限流器实现
 *
 */
@Configuration
public class RateLimiterConfig {

    @Autowired
    private FwRateLimitProperties properties;

    @Bean
    public RateLimiter rateLimiter() {
        if ("count".equals(properties.getEnable())) {
            return new RateLimiterRedisCount();
        } else if ("sliding".equals(properties.getEnable())) {
            return new RateLimiterRedisSlidingWindow();
        } else {
//            return new RateLimitRedisSlidingWindow();
            return new RateLimiterNone();
        }
    }

}
