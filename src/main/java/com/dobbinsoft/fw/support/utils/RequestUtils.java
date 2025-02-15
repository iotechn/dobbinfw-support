package com.dobbinsoft.fw.support.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;

public class RequestUtils {

    /**
     * 获取客户端IP
     * @param request 请求
     * @return
     */
    public static String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();


        String ipAddress = getHeaderValue(headers, "X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeaderValue(headers, "X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeaderValue(headers, "Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeaderValue(headers, "WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeaderValue(headers, "HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = getHeaderValue(headers, "HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddress().getHostName();
        }
        if (ipAddress.contains(",")) {
            // 阿里云SLB会将 LB的地址也传递过来，用逗号隔开。只需要取第一个即可
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public static String getHeaderValue(HttpHeaders headers, String header) {
        List<String> strings = headers.get(header);
        if (CollectionUtils.isEmpty(strings)) {
            return null;
        }
        return strings.getFirst();
    }

}
