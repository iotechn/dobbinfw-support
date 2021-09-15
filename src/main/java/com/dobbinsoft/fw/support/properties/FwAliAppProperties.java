package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2020/3/19
 * Time: 16:12
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.ALI_APP_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.ali-app")
public class FwAliAppProperties {

    private String aliGateway;

    private String miniAppId;

    private String miniAppPublicKey1;

    private String miniAppPublicKey2;

    private String miniAppPrivateKey2;

    private String appId;

    private String appPublicKey1;

    private String appPublicKey2;

    private String appPrivateKey2;

}
