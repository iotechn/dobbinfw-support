package com.dobbinsoft.fw.support.rate;

import com.dobbinsoft.fw.core.annotation.HttpMethod;

/**
 * ClassName: RateLimiterNone
 * Description: 完全不限流实现
 */
public class RateLimiterNone implements RateLimiter {

    @Override
    public boolean acquire(String fullMethod, HttpMethod httpMethod, Long personId, String ip) {
        return true;
    }

}
