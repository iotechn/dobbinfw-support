package com.dobbinsoft.fw.support.sms;

import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwSMSProperties;
import com.dobbinsoft.fw.support.sms.models.AliSendSMSResult;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import com.dobbinsoft.fw.support.utils.ali.AliCommonsRequest;
import com.dobbinsoft.fw.support.utils.ali.AliUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class AliyunSMSClient implements SMSClient {

    @Autowired
    private FwSMSProperties properties;

    private final Map<String, String> templateContentMap = new ConcurrentHashMap<>();

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


    private Map<String, String> parseTemplate(String templateId, String... args) throws ServiceException {
        String template = templateContentMap.get(templateId);
        if (template == null) {
            template = getTemplate(templateId);
            templateContentMap.put(templateId, template);
        }

        Map<String, String> resultMap = new HashMap<>();
        Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");
        Matcher matcher = pattern.matcher(template);

        int index = 0;
        while (matcher.find() && index < args.length) {
            String key = matcher.group(1);
            resultMap.put(key, args[index++]);
        }

        return resultMap;

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
        request.getQueryParam().put("TemplateParam", JacksonUtil.toJSONString(parseTemplate(templateId, args)));

        // 签名过程
        AliUtils.getAuthorization(request, properties.getAliyunAccessKeyId(), properties.getAliyunAccessKeySecret());
        // 调用API
        String json = AliUtils.callApi(request);
        if (StringUtils.isNotEmpty(json)) {
            AliSendSMSResult aliSendSMSResult = JacksonUtil.parseObject(json, AliSendSMSResult.class);
            if (!"OK".equalsIgnoreCase(aliSendSMSResult.getCode())) {
                log.error("[阿里云短信发送] 回复与预期不一致 request=" + JacksonUtil.toJSONString(request));
                log.error("[阿里云短信发送] 回复与预期不一致 result=" + json);
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

    private String getTemplate(String templateId) throws ServiceException {
        String httpMethod = "POST"; // 请求方式
        String canonicalUri = "/";
        String host = "dysmsapi.aliyuncs.com";  // endpoint
        String xAcsAction = "QuerySmsTemplate";  // API名称
        String xAcsVersion = "2017-05-25"; // API版本号
        AliCommonsRequest request = new AliCommonsRequest(httpMethod, canonicalUri, host, xAcsAction, xAcsVersion);

        // 调用API所需要的参数，参数按照参数名的字符代码升序排列，具有重复名称的参数应按值进行排序。
        request.getQueryParam().put("TemplateCode", templateId);

        // 签名过程
        AliUtils.getAuthorization(request, properties.getAliyunAccessKeyId(), properties.getAliyunAccessKeySecret());
        // 调用API
        String json = AliUtils.callApi(request);
        if (StringUtils.isNotBlank(json)) {
            AliTemplate aliTemplate = JacksonUtil.parseObject(json, AliTemplate.class);
            if (!"OK".equalsIgnoreCase(aliTemplate.getCode())) {
                log.error("[阿里云短信模板获取] 回复与预期不一致 request=" + JacksonUtil.toJSONString(request));
                log.error("[阿里云短信模板获取] 回复与预期不一致 result=" + json);
            }
            return aliTemplate.getTemplateContent() == null ? "" : aliTemplate.getTemplateContent();
        } else {
            log.info("[阿里云短信模板获取] 回复与预期不一致 request=" + JacksonUtil.toJSONString(request));
            log.info("[阿里云短信模板获取] 回复与预期不一致 result=" + json);
        }
        return "";
    }

    @NoArgsConstructor
    @Data
    private static class AliTemplate {
        @JsonProperty("TemplateCode")
        private String templateCode;
        @JsonProperty("RequestId")
        private String requestId;
        @JsonProperty("Message")
        private String message;
        @JsonProperty("TemplateContent")
        private String templateContent;
        @JsonProperty("TemplateName")
        private String templateName;
        @JsonProperty("TemplateType")
        private Integer templateType;
        @JsonProperty("Code")
        private String code;
        @JsonProperty("CreateDate")
        private String createDate;
        @JsonProperty("Reason")
        private String reason;
        @JsonProperty("TemplateStatus")
        private Integer templateStatus;
    }

}
