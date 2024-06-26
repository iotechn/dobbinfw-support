package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.captcha.CaptchaConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableCaptcha
 * Description: 是否激活滑动验证码，目前支持AjCaptcha
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CaptchaConfig.class)
@Documented
public @interface EnableCaptcha {
}
