package com.dobbinsoft.fw.support.rate;

import com.dobbinsoft.fw.core.annotation.HttpMethod;

/**
 * ClassName: RateLimiterNone
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-13
 */
public class RateLimiterNone implements RateLimiter {

    @Override
    public boolean acquire(String fullMethod, HttpMethod httpMethod, Long personId, String ip) {
        return true;
    }

}
