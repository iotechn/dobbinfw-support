package com.dobbinsoft.fw.support.config.sms;

import com.dobbinsoft.fw.support.sms.AliyunSMSClient;
import com.dobbinsoft.fw.support.sms.MockSMSClient;
import com.dobbinsoft.fw.support.sms.QCloudSMSClient;
import com.dobbinsoft.fw.support.sms.SMSClient;
import com.dobbinsoft.fw.support.properties.FwSMSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rize on 2019/7/2.
 */
@Configuration
public class SMSConfig {

    @Autowired
    private FwSMSProperties properties;

    @Bean
    public SMSClient smsClient() {
        if ("qcloud".equals(properties.getEnable())) {
            return new QCloudSMSClient();
        } else if ("aliyun".equals(properties.getEnable())) {
            return new AliyunSMSClient();
        } else if ("mock".equals(properties.getEnable())) {
            return new MockSMSClient();
        } else {
            return new MockSMSClient();
        }
    }
}
