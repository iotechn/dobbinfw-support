package com.dobbinsoft.fw.support.storage;

import lombok.Data;

import java.io.InputStream;

/**
 * ClassName: StorageRequest
 * Description: 上传对象请求
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
@Data
public class StorageRequest {

    private String filename;

    /**
     * eg: /images
     * eg: /css
     */
    private String path;

    private String contentType;

    private Long size;

    private InputStream is;

}
