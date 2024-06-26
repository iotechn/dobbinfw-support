package com.dobbinsoft.fw.support.sms;


import com.dobbinsoft.fw.core.exception.ServiceException;

import java.util.List;

/**
 * Created by rize on 2019/7/1.
 */
public interface SMSClient {

    public SMSResult sendRegisterVerify(String phone, String verifyCode) throws ServiceException;

    public SMSResult sendBindPhoneVerify(String phone, String verifyCode) throws ServiceException;

    public SMSResult sendResetPasswordVerify(String phone, String verifyCode) throws ServiceException;

    public SMSResult sendAdminLoginVerify(String phone, String verifyCode) throws ServiceException;

    /**
     * 发送任意类型的短信
     * @param phone 手机号
     * @param templateId 模板ID
     * @param args 替换参数
     * @return
     * @throws ServiceException
     */
    public SMSResult sendMsg(String phone, String templateId, String ...args) throws ServiceException;

    /**
     * 对多个手机号发送任意短信
     * @param phones
     * @param templateId
     * @param args
     * @return
     * @throws ServiceException
     */
    public SMSResult sendMsg(List<String> phones, String templateId, String ...args) throws ServiceException;

}
