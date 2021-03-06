package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2020/3/19
 * Time: 16:12
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.WX_APP_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.wx-app")
public class FwWxAppProperties {

    private String miniAppId;

    private String miniAppSecret;

    private String appId;

    private String appSecret;

    private String h5AppId;

    private String h5AppSecret;


}
