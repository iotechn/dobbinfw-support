package com.dobbinsoft.fw.support.storage;

import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: AliStorageClient
 * Description: 阿里云对象存储实现
 */
public class AliStorageClient extends S3StorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;


    @Override
    public String getAccessKeyId() {
        return properties.getAliAccessKeyId();
    }

    @Override
    public String getAccessKeySecret() {
        return properties.getAliAccessKeySecret();
    }

    @Override
    public String getBucketName() {
        return properties.getAliBucket();
    }

    @Override
    public String getBaseUrl() {
        return properties.getAliBaseUrl();
    }

    @Override
    public String getEndpoint() {
        return properties.getAliEndpoint();
    }

    @Override
    public String appendStyleForKey(String key, String style) {
        if (key.contains("?x-oss-process=image/")) {
            return key + "/" + style;
        } else {
            return key + "?x-oss-process=image/" + style;
        }
    }
}
