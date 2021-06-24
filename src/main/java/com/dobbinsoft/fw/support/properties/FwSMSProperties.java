package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:51
 */
@Data
//@DynamicConfigProperties(prefix = FwDynamicConst.SMS_CONFIG_PREFIX)
@Component
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

    private Integer qcloudAppId;

    private String qcloudAppKey;

    private String qcloudSignature;

    private Integer qcloudRegisterTemplateId;

    private Integer qcloudBindPhoneTemplateId;

    private Integer qcloudResetPasswordTemplateId;

    private Integer qcloudAdminLoginTemplateId;

}
