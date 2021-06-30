package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName: FwRateLimitProperties
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-13
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.RATE_LIMITER_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.rate-limit")
public class FwRateLimitProperties {

    private String enable;

}
