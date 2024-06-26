package com.dobbinsoft.fw.core.model;

import com.dobbinsoft.fw.core.annotation.doc.ApiField;
import lombok.Data;

/**
 * Description: 统一对外回复封装
 * @param <T> 数据类型
 */
@Data
public class GatewayResponse<T> {
    @ApiField(description = "200代表成功，非200代表失败")
    private int errno;
    @ApiField(description = "异常时的异常消息")
    private String errmsg;
    @ApiField(description = "返回数据")
    private T data;
    @ApiField(description = "HttpContentType")
    private String contentType;
    @ApiField(description = "系统时间戳")
    private long timestamp;

    public static <T> GatewayResponse<T> success(T t, String contentType) {
        GatewayResponse<T> response = new GatewayResponse<>();
        response.setErrno(200);
        response.setData(t);
        response.setContentType(contentType);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
}

