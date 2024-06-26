package com.dobbinsoft.fw.support.config.captcha;

import com.anji.captcha.service.CaptchaCacheService;
import com.dobbinsoft.fw.support.component.CacheComponent;
import org.springframework.beans.factory.annotation.Autowired;


public class RedisCaptchaCache implements CaptchaCacheService {

    private static final String CAPTCHA_PREFIX = "AJ_CAPTCHA_PREFIX:";

    @Autowired
    private CacheComponent cacheComponent;

    @Override
    public void set(String key, String value, long expiresInSeconds) {
        cacheComponent.putRaw(CAPTCHA_PREFIX + key, value, (int) expiresInSeconds);
    }

    @Override
    public boolean exists(String key) {
        return cacheComponent.hasKey(CAPTCHA_PREFIX + key);
    }

    @Override
    public void delete(String key) {
        cacheComponent.del(CAPTCHA_PREFIX + key);
    }

    @Override
    public String get(String key) {
        return cacheComponent.getRaw(CAPTCHA_PREFIX + key);
    }

    @Override
    public String type() {
        return "redis";
    }
}
