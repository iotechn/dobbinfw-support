package com.dobbinsoft.fw.support.config.captcha;

import com.dobbinsoft.fw.support.captcha.FwCaptchaClient;
import com.dobbinsoft.fw.support.captcha.QCloudFwCaptchaClient;
import com.dobbinsoft.fw.support.properties.FwCaptchaProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class CaptchaConfig {

    @Autowired
    private FwCaptchaProperties captchaProperties;

    @Bean
    public FwCaptchaClient captchaClient() {
        String enable = captchaProperties.getEnable();
        if ("qcloud".equals(enable)) {
            return new QCloudFwCaptchaClient();
        }
        return null;
    }

}
