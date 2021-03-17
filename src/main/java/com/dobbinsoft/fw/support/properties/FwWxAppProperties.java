package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2020/3/19
 * Time: 16:12
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.WX_APP_CONFIG_PREFIX)
public class FwWxAppProperties {

    private String miniAppId;

    private String miniAppSecret;

    private String appId;

    private String appSecret;

    private String h5AppId;

    private String h5AppSecret;


}
