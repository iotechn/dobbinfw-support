package com.dobbinsoft.fw.support.captcha;

public interface FwCaptchaClient {

    public boolean verify(String raw, String userIp);

}
