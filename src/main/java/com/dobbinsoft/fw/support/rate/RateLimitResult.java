package com.dobbinsoft.fw.support.rate;

import lombok.Data;

/**
 * ClassName: RateLimitResult
 * Description: 限流结果实例
 */
@Data
public class RateLimitResult {

    /**
     * 当前容量
     */
    private Integer current;

    /**
     * 最大容量
     */
    private Integer full;


}
