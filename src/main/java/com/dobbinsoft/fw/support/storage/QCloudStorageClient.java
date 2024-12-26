package com.dobbinsoft.fw.support.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
    public void afterPropertiesSet() throws Exception {
        // 实例加载
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(getAccessKeyId(), getAccessKeySecret())))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        "https://cos." + properties.getQcloudRegion() + ".myqcloud.com",
                        this.properties.getQcloudRegion()))
                .build();
    }

    @Override
    public String appendStyleForKey(String key, String style) {
        throw new RuntimeException("Not Support Now");
    }

    @Override
    public String appendVideoStyleForKey(String key, String style) {
        throw new RuntimeException("Not Support Now");
    }
}
