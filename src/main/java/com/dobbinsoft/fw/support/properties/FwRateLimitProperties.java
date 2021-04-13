package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * ClassName: FwRateLimitProperties
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-13
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.RATE_LIMITER_CONFIG_PREFIX)
public class FwRateLimitProperties {

    private String enable;

}
