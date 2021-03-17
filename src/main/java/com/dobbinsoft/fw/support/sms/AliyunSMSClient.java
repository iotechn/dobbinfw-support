package com.dobbinsoft.fw.support.sms;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.core.exception.ThirdPartServiceException;
import com.dobbinsoft.fw.support.properties.FwSMSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by rize on 2019/8/1.
 */
public class AliyunSMSClient implements SMSClient, InitializingBean {

    @Autowired
    private FwSMSProperties properties;

    private IAcsClient client;

    private static final Logger logger = LoggerFactory.getLogger(AliyunSMSClient.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = new DefaultAcsClient(DefaultProfile.getProfile("default", properties.getAliyunAccessKeyId(), properties.getAliyunAccessKeySecret()));
    }

    @Override
    public SMSResult sendRegisterVerify(String phone, String verifyCode) throws ServiceException {
        return sendCommon(phone, verifyCode, properties.getAliyunRegisterTemplateId(), properties.getAliyunSignature());
    }

    @Override
    public SMSResult sendBindPhoneVerify(String phone, String verifyCode) throws ServiceException {
        return sendCommon(phone, verifyCode, properties.getAliyunBindPhoneTemplateId(), properties.getAliyunSignature());
    }

    @Override
    public SMSResult sendResetPasswordVerify(String phone, String verifyCode) throws ServiceException {
        return sendCommon(phone, verifyCode, properties.getAliyunResetPasswordTemplateId(), properties.getAliyunSignature());
    }

    @Override
    public SMSResult sendAdminLoginVerify(String phone, String verifyCode) throws ServiceException {
        return sendCommon(phone, verifyCode, properties.getAliyunAdminLoginTemplateId(), properties.getAliyunSignature());
    }

    private SMSResult sendCommon(String phone, String verifyCode, String templateId, String signature) throws ServiceException {
        try {
            CommonRequest request = new CommonRequest();
            request.setMethod(MethodType.POST);
            request.setDomain("dysmsapi.aliyuncs.com");
            request.setVersion("2017-05-25");
            request.setAction("SendSms");
            request.putQueryParameter("RegionId", "default");
            request.putQueryParameter("PhoneNumbers", phone);
            request.putQueryParameter("SignName", signature);
            request.putQueryParameter("TemplateCode", templateId);
            request.putQueryParameter("TemplateParam", "{\"code\":\"" + verifyCode + "\"}");
            CommonResponse commonResponse = client.getCommonResponse(request);
            String data = commonResponse.getData();
            JSONObject jsonObject = JSONObject.parseObject(data);
            String message = jsonObject.getString("Message");
            SMSResult smsResult = new SMSResult();
            smsResult.setSucc("OK".equalsIgnoreCase(message));
            smsResult.setMsg(message);
            return smsResult;
        } catch (ClientException e) {
            throw new ThirdPartServiceException(e.getMessage(), CoreExceptionDefinition.THIRD_PART_SERVICE_EXCEPTION.getCode());
        } catch (Exception e) {
            logger.error("[阿里云短信发送] 异常", e);
            throw new ThirdPartServiceException(CoreExceptionDefinition.THIRD_PART_SERVICE_EXCEPTION);
        }
    }



}
