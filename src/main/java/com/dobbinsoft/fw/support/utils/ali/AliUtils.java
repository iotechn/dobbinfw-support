package com.dobbinsoft.fw.support.utils.ali;

import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@Slf4j
public class AliUtils {

    // 以下调用方法，可在其他云产品中使用

    /**
     * 签名协议
     */
    private static final String ALGORITHM = "ACS3-HMAC-SHA256";

    public static String callApi(AliCommonsRequest request) throws ServiceException {
        try {
            // 通过HttpClient发送请求
            String url = "https://" + request.getHost() + request.getCanonicalUri();
            URIBuilder uriBuilder = new URIBuilder(url);
            // 添加请求参数
            for (Map.Entry<String, Object> entry : request.getQueryParam().entrySet()) {
                uriBuilder.addParameter(entry.getKey(), String.valueOf(entry.getValue()));
            }
            HttpUriRequest httpRequest;
            switch (request.getHttpMethod()) {
                case "GET":
                    httpRequest = new HttpGet(uriBuilder.build());
                    break;
                case "POST":
                    HttpPost httpPost = new HttpPost(uriBuilder.build());
                    if (request.getBody() != null) {
                        StringEntity postEntity = new StringEntity(request.getBody());
                        httpPost.setEntity(postEntity);
                    }
                    httpRequest = httpPost;
                    break;
                case "DELETE":
                    httpRequest = new HttpDelete(uriBuilder.build());
                    break;
                case "PUT":
                    HttpPut httpPut = new HttpPut(uriBuilder.build());
                    if (request.getBody() != null) {
                        StringEntity putEntity = new StringEntity(request.getBody());
                        httpPut.setEntity(putEntity);
                    }
                    httpRequest = httpPut;
                    break;
                default:
                    log.error("Unsupported HTTP method: " + request.getBody());
                    throw new IllegalArgumentException("Unsupported HTTP method");
            }

            // 添加http请求头
            for (Map.Entry<String, Object> entry : request.getHeaders().entrySet()) {
                httpRequest.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
            }
            // 发送请求
            try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                log.info("[阿里云短信发送] 请求结果: {}", result);
                return result;
            }
        } catch (IOException e) {
            // 异常处理
            log.error("[阿里云短信发送] 网络异常", e);
            throw new ServiceException("阿里云短信发送业务异常", CoreExceptionDefinition.THIRD_PART_IO_EXCEPTION.getCode());
        } catch (Exception e) {
            // 异常处理
            log.error("[阿里云短信发送] 异常", e);
            throw new ServiceException("阿里云短信发送业务异常", CoreExceptionDefinition.THIRD_PART_SERVICE_EXCEPTION.getCode());
        }
    }

    /**
     * 该方法用于根据传入的HTTP请求方法、规范化的URI、查询参数等，计算并生成授权信息。
     */
    public static void getAuthorization(AliCommonsRequest request, String accessKeyId, String accessKeySecret) {
        try {
            // 步骤 1：拼接规范请求串
            // 请求参数，当请求的查询字符串为空时，使用空字符串作为规范化查询字符串
            StringBuilder canonicalQueryString = new StringBuilder();
            request.getQueryParam().entrySet().stream().map(entry -> percentCode(entry.getKey()) + "=" + percentCode(String.valueOf(entry.getValue()))).forEachOrdered(queryPart -> {
                // 如果canonicalQueryString已经不是空的，则在新查询参数前添加"&"
                if (!canonicalQueryString.isEmpty()) {
                    canonicalQueryString.append("&");
                }
                canonicalQueryString.append(queryPart);
            });

            // 请求体，当请求正文为空时，比如GET请求，RequestPayload固定为空字符串
            String requestPayload = "";
            if (request.getBody() != null) {
                requestPayload = request.getBody();
            }

            // 计算请求体的哈希值
            String hashedRequestPayload = sha256Hex(requestPayload);
            request.getHeaders().put("x-acs-content-sha256", hashedRequestPayload);
            // 构造请求头，多个规范化消息头，按照消息头名称（小写）的字符代码顺序以升序排列后拼接在一起
            StringBuilder canonicalHeaders = new StringBuilder();
            // 已签名消息头列表，多个请求头名称（小写）按首字母升序排列并以英文分号（;）分隔
            StringBuilder signedHeadersSb = new StringBuilder();
            request.getHeaders().entrySet().stream().filter(entry -> entry.getKey().toLowerCase().startsWith("x-acs-") || entry.getKey().equalsIgnoreCase("host") || entry.getKey().equalsIgnoreCase("content-type")).sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                String lowerKey = entry.getKey().toLowerCase();
                String value = String.valueOf(entry.getValue()).trim();
                canonicalHeaders.append(lowerKey).append(":").append(value).append("\n");
                signedHeadersSb.append(lowerKey).append(";");
            });
            String signedHeaders = signedHeadersSb.substring(0, signedHeadersSb.length() - 1);
            String canonicalRequest = request.getHttpMethod() + "\n" + request.getCanonicalUri() + "\n" + canonicalQueryString + "\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;
//            System.out.println("canonicalRequest=========>\n" + canonicalRequest);

            // 步骤 2：拼接待签名字符串
            String hashedCanonicalRequest = sha256Hex(canonicalRequest); // 计算规范化请求的哈希值
            String stringToSign = ALGORITHM + "\n" + hashedCanonicalRequest;

            // 步骤 3：计算签名


            String signature = Hex.encodeHexString(hmac256(accessKeySecret.getBytes(StandardCharsets.UTF_8), stringToSign)).toLowerCase();
//            System.out.println("signature=========>" + signature);

            // 步骤 4：拼接 Authorization
            String authorization = ALGORITHM + " " + "Credential=" + accessKeyId + ",SignedHeaders=" + signedHeaders + ",Signature=" + signature;
            request.getHeaders().put("Authorization", authorization);
        } catch (Exception e) {
            // 异常处理
            log.error("[阿里云API签名] 异常", e);
        }
    }

    /**
     * 使用HmacSHA256算法生成消息认证码（MAC）。
     *
     * @param key 密钥，用于生成MAC的密钥，必须保密。
     * @param str 需要进行MAC认证的消息。
     * @return 返回使用HmacSHA256算法计算出的消息认证码。
     * @throws Exception 如果初始化MAC或计算MAC过程中遇到错误，则抛出异常。
     */
    public static byte[] hmac256(byte[] key, String str) throws Exception {
        // 实例化HmacSHA256消息认证码生成器
        Mac mac = Mac.getInstance("HmacSHA256");
        // 创建密钥规范，用于初始化MAC生成器
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        // 初始化MAC生成器
        mac.init(secretKeySpec);
        // 计算消息认证码并返回
        return mac.doFinal(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用SHA-256算法计算字符串的哈希值并以十六进制字符串形式返回。
     *
     * @param str 需要进行SHA-256哈希计算的字符串。
     * @return 计算结果为小写十六进制字符串。
     * @throws Exception 如果在获取SHA-256消息摘要实例时发生错误。
     */
    public static String sha256Hex(String str) throws Exception {
        // 获取SHA-256消息摘要实例
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // 计算字符串s的SHA-256哈希值
        byte[] d = md.digest(str.getBytes(StandardCharsets.UTF_8));
        // 将哈希值转换为小写十六进制字符串并返回
        return Hex.encodeHexString(d).toLowerCase();
    }

    /**
     * 对指定的字符串进行URL编码。
     * 使用UTF-8编码字符集对字符串进行编码，并对特定的字符进行替换，以符合URL编码规范。
     *
     * @param str 需要进行URL编码的字符串。
     * @return 编码后的字符串。其中，加号"+"被替换为"%20"，星号"*"被替换为"%2A"，波浪号"%7E"被替换为"~"。
     */
    public static String percentCode(String str) {
        if (str == null) {
            throw new IllegalArgumentException("输入字符串不可为null");
        }
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8编码不被支持", e);
        }
    }

}
