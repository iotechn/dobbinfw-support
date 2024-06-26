package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:42
 */
@Data
@Configuration
@ConfigurationProperties(prefix = FwCaptchaProperties.PREFIX)
public class FwCaptchaProperties {

    public static final String PREFIX = "com.dobbinsoft.captcha";

    private String enable;

    private String qcloudSecretId;

    private String qcloudSecretKey;

    private Long qcloudAppId;

    private String qcloudAppSecretKey;

}
