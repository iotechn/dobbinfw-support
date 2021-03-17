package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:12
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.WX_PAY_CONFIG_PREFIX)
public class FwWxPayProperties {

    private String mchId;

    private String mchKey;

    private String notifyUrl;

    private String keyPath;

}
