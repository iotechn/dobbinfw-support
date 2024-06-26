package com.dobbinsoft.fw.support.storage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageInfoResult {

    // Key 是否存在
    private Boolean exist;

    // Key 值
    private String key;

    // 字节数
    private Long contentLength;

    // 类型
    private String contentType;

}
