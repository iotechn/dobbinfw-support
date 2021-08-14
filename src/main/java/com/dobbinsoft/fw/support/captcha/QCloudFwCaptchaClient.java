package com.dobbinsoft.fw.support.captcha;

import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.captcha.v20190722.CaptchaClient;
import com.dobbinsoft.fw.support.captcha.v20190722.models.DescribeCaptchaResultRequest;
import com.dobbinsoft.fw.support.captcha.v20190722.models.DescribeCaptchaResultResponse;
import com.dobbinsoft.fw.support.properties.FwCaptchaProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QCloudFwCaptchaClient implements FwCaptchaClient {

    @Autowired
    private FwCaptchaProperties captchaProperties;

    private static final Logger logger = LoggerFactory.getLogger(QCloudFwCaptchaClient.class);

    @Override
    public boolean verify(String raw, String userIp) {
        try{
            JSONObject jsonObject = JSONObject.parseObject(raw);
            String ticket = jsonObject.getString("ticket");
            String randStr = jsonObject.getString("randStr");
            // 实例化一个认证对象，入参需要传入腾讯云账户secretId，secretKey,此处还需注意密钥对的保密
            // 密钥可前往https://console.cloud.tencent.com/cam/capi网站进行获取
            Credential cred = new Credential(this.captchaProperties.getQcloudSecretId(), this.captchaProperties.getQcloudSecretKey());
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("captcha.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            CaptchaClient client = new CaptchaClient(cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeCaptchaResultRequest req = new DescribeCaptchaResultRequest();
            req.setCaptchaType(9L);
            req.setTicket(ticket);
            req.setUserIp(userIp);
            req.setRandstr(randStr);
            req.setCaptchaAppId(this.captchaProperties.getQcloudAppId());
            req.setAppSecretKey(this.captchaProperties.getQcloudAppSecretKey());
            // 返回的resp是一个DescribeCaptchaResultResponse的实例，与请求对象对应
            DescribeCaptchaResultResponse resp = client.DescribeCaptchaResult(req);
            // 输出json格式的字符串回包
            Long captchaCode = resp.getCaptchaCode();
            return captchaCode.intValue() == 1;
        } catch (Exception e) {
            logger.error("[腾讯云] 滑动验证码校验 异常", e);
        }
        return false;
    }
}
