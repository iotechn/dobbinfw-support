package com.dobbinsoft.fw.support.sms;

import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2019/11/17
 * Time: 15:50
 */
public class MockSMSClient implements SMSClient {

    private static final Logger logger = LoggerFactory.getLogger(MockSMSClient.class);

    @Override
    public SMSResult sendRegisterVerify(String phone, String verifyCode) throws ServiceException {
        return send(phone, verifyCode);
    }

    @Override
    public SMSResult sendBindPhoneVerify(String phone, String verifyCode) throws ServiceException {
        return send(phone, verifyCode);
    }

    @Override
    public SMSResult sendResetPasswordVerify(String phone, String verifyCode) throws ServiceException {
        return send(phone, verifyCode);
    }

    @Override
    public SMSResult sendAdminLoginVerify(String phone, String verifyCode) throws ServiceException {
        return send(phone, verifyCode);
    }

    @Override
    public SMSResult sendMsg(String phone, String templateId, String... args) throws ServiceException {
        logger.info("[模拟短信发送] phone=" + phone + "; templateId=" + templateId + "; args=" + JacksonUtil.toJSONString(args));
        SMSResult smsResult = new SMSResult();
        smsResult.setSuccess(true);
        smsResult.setMessage("OK");
        return smsResult;
    }

    @Override
    public SMSResult sendMsg(List<String> phones, String templateId, String... args) throws ServiceException {
        logger.info("[模拟短信发送] phones=" + String.join(",", phones) + "; templateId=" + templateId + "; args=" + JacksonUtil.toJSONString(args));
        SMSResult smsResult = new SMSResult();
        smsResult.setSuccess(true);
        smsResult.setMessage("OK");
        return smsResult;
    }

    public SMSResult send(String phone, String verifyCode) {
        logger.info("[模拟短信发送] phone=" + phone + "; verifyCode=" + verifyCode);
        SMSResult smsResult = new SMSResult();
        smsResult.setSuccess(true);
        smsResult.setMessage("OK");
        return smsResult;
    }
}
