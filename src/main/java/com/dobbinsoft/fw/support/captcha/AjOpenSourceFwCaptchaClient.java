package com.dobbinsoft.fw.support.captcha;


import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AjOpenSourceFwCaptchaClient implements FwCaptchaClient {

    @Autowired
    private CaptchaService captchaService;

    private static final Logger logger = LoggerFactory.getLogger(AjOpenSourceFwCaptchaClient.class);

    @Override
    public boolean verify(String raw, String userIp) {
        CaptchaVO captchaVO = JSONObject.parseObject(raw, CaptchaVO.class);
        ResponseModel verification = captchaService.verification(captchaVO);
        if (!verification.isSuccess()) {
            logger.error("[用户登录] 验证码错误：repCode=" + verification.getRepCode());
            return false;
        }
        return true;
    }
}
