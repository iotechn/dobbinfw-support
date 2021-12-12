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

    private String miniNotifyUrl;

    private String appId;

    private String appPublicKey1;

    private String appPublicKey2;

    private String appPrivateKey2;

    private String appNotifyUrl;

    private String webAppId;

    private String webAppPublicKey1;

    private String webAppPublicKey2;

    private String webAppPrivateKey2;

    private String webNotifyUrl;

    private String webReturnUrl;

    private String wapAppId;

    private String wapAppPublicKey1;

    private String wapAppPublicKey2;

    private String wapAppPrivateKey2;

    private String wapNotifyUrl;

    private String wapReturnUrl;

}
