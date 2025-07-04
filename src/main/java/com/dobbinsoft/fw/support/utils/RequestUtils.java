package com.dobbinsoft.fw.support.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtils {

    /**
     * 获取当前 HttpServletRequest
     *
     * @return HttpServletRequest 对象
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        return attributes.getRequest();
    }

    /**
     * 获取请求头中的参数
     *
     * @param headerName 请求头名称
     * @return 请求头的值
     */
    public static String getHeader(String headerName) {
        HttpServletRequest request = getCurrentRequest();
        return request.getHeader(headerName);
    }

    /**
     * 获取请求参数
     *
     * @param paramName 参数名称
     * @return 参数值
     */
    public static String getParameter(String paramName) {
        HttpServletRequest request = getCurrentRequest();
        return request.getParameter(paramName);
    }

    /**
     * 获取客户端IP
     * @param request 请求
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress.contains(",")) {
            // 阿里云SLB会将 LB的地址也传递过来，用逗号隔开。只需要取第一个即可
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

}
