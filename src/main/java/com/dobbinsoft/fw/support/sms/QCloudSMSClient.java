package com.dobbinsoft.fw.support.sms;

import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwSMSProperties;
import com.dobbinsoft.fw.support.sms.models.QCloudSendSMSRequest;
import com.dobbinsoft.fw.support.sms.models.QCloudSendSMSResponse;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by rize on 2019/7/1.
 */
public class QCloudSMSClient implements SMSClient, InitializingBean {


    @Autowired
    private FwSMSProperties properties;

    private OkHttpClient client;

    private static final Logger logger = LoggerFactory.getLogger(QCloudSMSClient.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        this.client = new OkHttpClient();
    }

    @Override
    public SMSResult sendMsg(String phone, String templateId, String... args) throws ServiceException {
        return sendMsg(Arrays.asList(phone), templateId, args);
    }

    @Override
    public SMSResult sendMsg(List<String> phones, String templateId, String... args) throws ServiceException {
        try {
            QCloudSendSMSRequest sendSMSRequest = new QCloudSendSMSRequest();
            sendSMSRequest.setSmsSdkAppId(properties.getQcloudAppId());
            sendSMSRequest.setPhoneNumberSet(phones);
            sendSMSRequest.setSignName(properties.getQcloudSignature());
            sendSMSRequest.setTemplateId(templateId);
            sendSMSRequest.setTemplateParamSet(Arrays.asList(args));
            sendSMSRequest.setSessionContext("");
            String body = JacksonUtil.toJSONString(sendSMSRequest);
            String resp = doRequest(properties.getQcloudSecretId(), properties.getQcloudSecretKey(),
                    "sms",
                    "2021-01-11",
                    "SendSms",
                    body,
                    StringUtils.isEmpty(properties.getQcloudRegion()) ? "ap-guangzhou" : "",
                    "");
            QCloudSendSMSResponse qCloudSendSMSResponse = JacksonUtil.parseObject(resp, QCloudSendSMSResponse.class);
            QCloudSendSMSResponse.ResponseDTO response = qCloudSendSMSResponse.getResponse();
            if (CollectionUtils.isNotEmpty(response.getSendStatusSet())) {
                if (StringUtils.equalsIgnoreCase(response.getSendStatusSet().get(0).getCode(), "OK")) {
                    SMSResult smsResult = new SMSResult();
                    smsResult.setSuccess(true);
                    smsResult.setMessage("成功");
                    return smsResult;
                }
            }
            logger.info("[腾讯短信发送] 回复与预期不一致 request=" + body);
            logger.info("[腾讯短信发送] 回复与预期不一致 result=" + resp);
            throw new ServiceException("腾讯云短信发送业务异常", CoreExceptionDefinition.THIRD_PART_IO_EXCEPTION.getCode());
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("[腾讯短信发送] 异常", e);
            throw new ServiceException("腾讯云短信发送未知异常", CoreExceptionDefinition.THIRD_PART_SERVICE_EXCEPTION.getCode());
        }
    }


    @Override
    public SMSResult sendRegisterVerify(String phone, String verifyCode) throws ServiceException {
        return sendMsg(phone, properties.getQcloudRegisterTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendBindPhoneVerify(String phone, String verifyCode) throws ServiceException {
        return sendMsg(phone, properties.getQcloudBindPhoneTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendResetPasswordVerify(String phone, String verifyCode) throws ServiceException {
        return sendMsg(phone, properties.getQcloudResetPasswordTemplateId(), verifyCode);
    }

    @Override
    public SMSResult sendAdminLoginVerify(String phone, String verifyCode) throws ServiceException {
        return sendMsg(phone, properties.getQcloudAdminLoginTemplateId(), verifyCode);
    }

    public String doRequest(
            String secretId, String secretKey,
            String service, String version, String action,
            String body, String region, String token
    ) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        Request request = buildRequest(secretId, secretKey, service, version, action, body, region, token);
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public Request buildRequest(
            String secretId, String secretKey,
            String service, String version, String action,
            String body, String region, String token
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        String host = "sms.tencentcloudapi.com";
        String endpoint = "https://" + host;
        String contentType = "application/json; charset=utf-8";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String auth = getAuth(secretId, secretKey, host, contentType, timestamp, body);
        return new Request.Builder()
                .header("Host", host)
                .header("X-TC-Timestamp", timestamp)
                .header("X-TC-Version", version)
                .header("X-TC-Action", action)
                .header("X-TC-Region", region)
                .header("X-TC-Token", token)
                .header("X-TC-RequestClient", "SDK_JAVA_BAREBONE")
                .header("Authorization", auth)
                .url(endpoint)
                .post(RequestBody.create(MediaType.parse(contentType), body))
                .build();
    }

    private String getAuth(
            String secretId, String secretKey, String host, String contentType,
            String timestamp, String body
    ) throws NoSuchAlgorithmException, InvalidKeyException {
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:" + contentType + "\nhost:" + host + "\n";
        String signedHeaders = "content-type;host";

        String hashedRequestPayload = sha256Hex(body.getBytes(StandardCharsets.UTF_8));
        String canonicalRequest = "POST"
                + "\n"
                + canonicalUri
                + "\n"
                + canonicalQueryString
                + "\n"
                + canonicalHeaders
                + "\n"
                + signedHeaders
                + "\n"
                + hashedRequestPayload;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(Long.valueOf(timestamp + "000")));
        String service = host.split("\\.")[0];
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest =
                sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));
        String stringToSign =
                "TC3-HMAC-SHA256\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmac256(secretDate, service);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature =
                printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();
        return "TC3-HMAC-SHA256 "
                + "Credential="
                + secretId
                + "/"
                + credentialScope
                + ", "
                + "SignedHeaders="
                + signedHeaders
                + ", "
                + "Signature="
                + signature;
    }

    public String sha256Hex(byte[] b) throws NoSuchAlgorithmException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(b);
        return printHexBinary(d).toLowerCase();
    }

    private final char[] hexCode = "0123456789ABCDEF".toCharArray();

    public String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    public byte[] hmac256(byte[] key, String msg) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

}
