package com.dobbinsoft.fw.support.rate;

import com.dobbinsoft.fw.core.annotation.HttpMethod;

/**
 * ClassName: RateLimiter
 * Description: 浏览限制器
 *
 * @author: e-weichaozheng
 * @date: 2021-04-12
 */
public interface RateLimiter {

    /**
     * 获取流量通行许可
     * @param fullMethod 方法全名 group.method
     * @param httpMethod 方法注解
     * @param personId 人的ID，管理员 或 用户
     * @param ip 外网IP
     * @return true 代表允许流量通过
     */
    public boolean acquire(String fullMethod, HttpMethod httpMethod, Long personId, String ip);

}
