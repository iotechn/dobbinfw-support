package com.dobbinsoft.fw.support.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

/**
 * ClassName: AliStorageClient
 * Description: 阿里云对象存储实现
 */
public class AliStorageClient extends S3StorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;

    private volatile OkHttpClient okHttpClient;


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

    // 阿里云兼容不了S3
    @Override
    public InputStream download(String key) {
        String privateUrl = getPrivateUrl(key, 360);
        if (okHttpClient == null) {
            synchronized (this) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient();
                }
            }
        }
        try {
            Response response = okHttpClient.newCall(new Request.Builder()
                    .url(privateUrl)
                    .build()).execute();
            return response.body().byteStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
