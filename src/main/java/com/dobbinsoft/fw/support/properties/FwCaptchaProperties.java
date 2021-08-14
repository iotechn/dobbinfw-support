package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:42
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.OSS_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.captcha")
public class FwCaptchaProperties {

    private String enable;

    private String qcloudSecretId;

    private String qcloudSecretKey;

    private Long qcloudAppId;

    private String qcloudAppSecretKey;

}
