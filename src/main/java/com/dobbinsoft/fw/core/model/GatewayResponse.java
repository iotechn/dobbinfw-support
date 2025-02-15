package com.dobbinsoft.fw.core.model;

import com.dobbinsoft.fw.core.annotation.doc.ApiField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.MediaType;

import java.util.Map;

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
    @ApiField(description = "系统时间戳")
    private long timestamp;

    @JsonIgnore
    @ApiField(description = "HttpContentType")
    private MediaType contentType;
    @JsonIgnore
    @ApiField(description = "前端忽略")
    private Map<String, String> httpHeaders;

}

