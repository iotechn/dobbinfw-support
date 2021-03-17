package com.dobbinsoft.fw.support.storage;

import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: QCloudStorageClient
 * Description: 腾讯云对象存储客户端
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public class QCloudStorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;

    private COSClient cosClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        COSCredentials cred = new BasicCOSCredentials(properties.getQcloudSecretId(), properties.getQcloudSecretKey());
        // 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(properties.getQcloudRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        this.cosClient = new COSClient(cred, clientConfig);
    }

    @Override
    public StorageResult save(StorageRequest request) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(request.getSize());
        objectMetadata.setContentType(request.getContentType());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                properties.getQcloudBucket(),
                request.getPath() + "/" + request.getFilename(),
                request.getIs(), objectMetadata);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

        StorageResult result = new StorageResult();
        result.setSuc(true);
        result.setUrl(properties.getQcloudBaseUrl() + request.getPath() + "/" + request.getFilename());
        return result;
    }
}
