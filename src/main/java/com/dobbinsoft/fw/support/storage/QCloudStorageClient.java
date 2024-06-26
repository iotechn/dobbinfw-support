package com.dobbinsoft.fw.support.storage;

import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: QCloudStorageClient
 * Description: 腾讯云对象存储客户端
 */
public class QCloudStorageClient extends S3StorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;


    @Override
    public String getAccessKeyId() {
        return properties.getQcloudSecretId();
    }

    @Override
    public String getAccessKeySecret() {
        return properties.getQcloudSecretKey();
    }

    @Override
    public String getBucketName() {
        return properties.getQcloudBucket();
    }

    @Override
    public String getBaseUrl() {
        return properties.getQcloudBaseUrl();
    }

    @Override
    public String getEndpoint() {
        return properties.getQcloudBucket() + ".cos." + properties.getQcloudRegion() + ".myqcloud.com";
    }

    @Override
    public String appendStyleForKey(String key, String style) {
        throw new RuntimeException("Not Support Now");
    }
}
