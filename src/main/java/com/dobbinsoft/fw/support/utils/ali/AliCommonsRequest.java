package com.dobbinsoft.fw.support.utils.ali;

import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.UUID;


@Getter
public class AliCommonsRequest {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    // HTTP Method
    private final String httpMethod;
    // 请求路径，当资源路径为空时，使用正斜杠(/)作为CanonicalURI
    private final String canonicalUri;
    // endpoint
    private final String host;
    // API name
    private final String xAcsAction;
    // API version
    private final String xAcsVersion;
    // headers
    TreeMap<String, Object> headers = new TreeMap<>();
    // 调用API所需要的参数，参数位置在body。Json字符串
    String body;
    // 调用API所需要的参数，参数位置在query，参数按照参数名的字符代码升序排列
    TreeMap<String, Object> queryParam = new TreeMap<>();

    public AliCommonsRequest(String httpMethod, String canonicalUri, String host, String xAcsAction, String xAcsVersion) {
        this.httpMethod = httpMethod;
        this.canonicalUri = canonicalUri;
        this.host = host;
        this.xAcsAction = xAcsAction;
        this.xAcsVersion = xAcsVersion;
        initBuilder();
    }

    // init headers
    private void initBuilder() {
        headers.put("host", host);
        headers.put("x-acs-action", xAcsAction);
        headers.put("x-acs-version", xAcsVersion);
        SDF.setTimeZone(new SimpleTimeZone(0, "GMT")); // 设置日期格式化时区为GMT
        headers.put("x-acs-date", SDF.format(new Date()));
        headers.put("x-acs-signature-nonce", UUID.randomUUID().toString());
    }


}
