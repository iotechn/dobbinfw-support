package com.dobbinsoft.fw.support.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ClassName: AliStorageClient
 * Description: 阿里云对象存储实现
 */
public class AliStorageClient extends S3StorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;

    private volatile WebClient webClient;


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
    public String getRuntimeBucketName() {
        // 阿里云使用 PathStyleAccessEnabled 时 bucketName必须传空, 在save/delete等情况
        return "";
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
    public void afterPropertiesSet() throws Exception {
        // 实例加载
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(getAccessKeyId(), getAccessKeySecret())))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        this.getBaseUrl(),
                        ""))
                // 这是一组配置
//                .withPathStyleAccessEnabled(false)
//                .withChunkedEncodingDisabled(true)
                // 这是一种配置
                .withPathStyleAccessEnabled(true)
                .build();
    }

    @Override
    public String appendStyleForKey(String key, String style) {
        if (key.contains("?x-oss-process=image/")) {
            return key + "/" + style;
        } else {
            return key + "?x-oss-process=image/" + style;
        }
    }

    @Override
    public String appendVideoStyleForKey(String key, String style) {
        if (key.contains("?x-oss-process=video/")) {
            return key + "/" + style;
        } else {
            return key + "?x-oss-process=video/" + style;
        }
    }

    // 阿里云兼容不了S3
    @Override
    public InputStream download(String key) {
        String privateUrl = getPrivateUrl(key, 360);
        if (webClient == null) {
            synchronized (this) {
                if (webClient == null) {
                    webClient = WebClient.create();
                }
            }
        }
        byte[] responseBytes = webClient.get()
                .uri(privateUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (responseBytes == null) {
            throw new RuntimeException("响应体为空");
        }
        return new ByteArrayInputStream(responseBytes);
    }
}
