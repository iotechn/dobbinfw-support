package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:51
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.sms")
public class FwSMSProperties {

    private String enable;

    private String aliyunAccessKeyId;

    private String aliyunAccessKeySecret;

    private String aliyunSignature;

    private String aliyunRegisterTemplateId;

    private String aliyunBindPhoneTemplateId;

    private String aliyunResetPasswordTemplateId;

    private String aliyunAdminLoginTemplateId;

    private String qcloudAppId;

    private String qcloudAppKey;

    private String qcloudSecretId;

    private String qcloudSecretKey;

    private String qcloudSignature;

    private String qcloudRegisterTemplateId;

    private String qcloudBindPhoneTemplateId;

    private String qcloudResetPasswordTemplateId;

    private String qcloudAdminLoginTemplateId;

}
