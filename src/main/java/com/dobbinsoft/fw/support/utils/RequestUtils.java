package com.dobbinsoft.fw.support.utils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.socket.HandshakeInfo;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestUtils {

    /**
     * 获取客户端IP
     * @param request 请求
     * @return
     */
    public static String getClientIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ipAddress = tryGetIpFromHeader(headers, request.getRemoteAddress());
        if (ipAddress.contains(",")) {
            // 阿里云SLB会将 LB的地址也传递过来，用逗号隔开。只需要取第一个即可
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    /**
     * 获取客户端IP
     * @param handshakeInfo 握手请求
     * @return
     */
    public static String getWsClientIp(HandshakeInfo handshakeInfo) {
        HttpHeaders headers = handshakeInfo.getHeaders();
        String ipAddress = tryGetIpFromHeader(headers, handshakeInfo.getRemoteAddress());
        if (ipAddress.contains(",")) {
            // 阿里云SLB会将 LB的地址也传递过来，用逗号隔开。只需要取第一个即可
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    private static String tryGetIpFromHeader(HttpHeaders headers, InetSocketAddress handshakeInfo) {
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
            ipAddress = handshakeInfo.getHostName();
        }
        return ipAddress;
    }

    /**
     * 获取Header中的值
     * @param headers
     * @param header
     * @return
     */
    public static String getHeaderValue(HttpHeaders headers, String header) {
        List<String> strings = headers.get(header);
        if (CollectionUtils.isEmpty(strings)) {
            return null;
        }
        return strings.getFirst();
    }

    /**
     * 提取Query中的参数
     * @param uri 例如传入·api?param1=xxx
     * @return {"param1":"xxx"}
     */
    public static Map<String, String> extractQueryParams(URI uri) {
        Map<String, String> paramsMap = new HashMap<>();
        // 解析URL
        String query = uri.getQuery();
        // 如果URL有查询字符串
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    paramsMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return paramsMap;
    }

}
