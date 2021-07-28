package com.dobbinsoft.fw.support.captcha;

import org.springframework.stereotype.Component;

@Component
public class QCloudCaptchaClient implements CaptchaClient {


    @Override
    public boolean verify(String raw) {
        return false;
    }
}
