package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.captcha.CaptchaConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableDynamicConfig
 * Description: EnableDynamicConfig
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CaptchaConfig.class)
@Documented
public @interface EnableCaptcha {
}
