package com.dobbinsoft.fw.support.sms;

import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwSMSProperties;
import com.dobbinsoft.fw.support.utils.ali.AliCommonsRequest;
import com.dobbinsoft.fw.support.sms.models.AliSendSMSResult;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import com.dobbinsoft.fw.support.utils.ali.AliUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Slf4j
public class AliyunSMSClient implements SMSClient {

    @Autowired
    private FwSMSProperties properties;

    @Override
    public SMSResult sendRegisterVerify(String phone, String verifyCode) throws ServiceException {
        return this.sendMsg(phone, properties.getAliyunRegisterTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendBindPhoneVerify(String phone, String verifyCode) throws ServiceException {
        return this.sendMsg(phone, properties.getAliyunBindPhoneTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendResetPasswordVerify(String phone, String verifyCode) throws ServiceException {
        return this.sendMsg(phone, properties.getAliyunResetPasswordTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendAdminLoginVerify(String phone, String verifyCode) throws ServiceException {
        return this.sendMsg(phone, properties.getAliyunAdminLoginTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendMsg(String phone, String templateId, String... args) throws ServiceException {
        return this.sendMsg(Arrays.asList(phone), templateId, args);
    }

    @Override
    public SMSResult sendMsg(List<String> phones, String templateId, String... args) throws ServiceException {
        String httpMethod = "POST"; // 请求方式
        String canonicalUri = "/";
        String host = "dysmsapi.aliyuncs.com";  // endpoint
        String xAcsAction = "SendSms";  // API名称
        String xAcsVersion = "2017-05-25"; // API版本号
        AliCommonsRequest request = new AliCommonsRequest(httpMethod, canonicalUri, host, xAcsAction, xAcsVersion);

        // 调用API所需要的参数，参数按照参数名的字符代码升序排列，具有重复名称的参数应按值进行排序。
        request.getQueryParam().put("PhoneNumbers", String.join(",", phones));
        request.getQueryParam().put("SignName", properties.getAliyunSignature());
        request.getQueryParam().put("TemplateCode", templateId);
        request.getQueryParam().put("TemplateParam", "{\"code\":\"" + args[0] + "\"}");

        // 签名过程
        AliUtils.getAuthorization(request, properties.getAliyunAccessKeyId(), properties.getAliyunAccessKeySecret());
        // 调用API
        String json = AliUtils.callApi(request);
        if (StringUtils.isNotEmpty(json)) {
            AliSendSMSResult aliSendSMSResult = JacksonUtil.parseObject(json, AliSendSMSResult.class);
            if (!"OK".equalsIgnoreCase(aliSendSMSResult.getCode())) {
                log.info("[阿里云短信发送] 回复与预期不一致 request=" + JacksonUtil.toJSONString(request));
                log.info("[阿里云短信发送] 回复与预期不一致 result=" + json);
            }
            SMSResult smsResult = new SMSResult();
            smsResult.setMessage(aliSendSMSResult.getMessage());
            smsResult.setSuccess("OK".equalsIgnoreCase(aliSendSMSResult.getCode()));
            return smsResult;
        } else {
            log.info("[阿里云短信发送] 回复与预期不一致 request=" + JacksonUtil.toJSONString(request));
            log.info("[阿里云短信发送] 回复与预期不一致 result=" + json);
        }
        SMSResult smsResult = new SMSResult();
        smsResult.setMessage("失败");
        smsResult.setSuccess(false);
        return smsResult;
    }

}
