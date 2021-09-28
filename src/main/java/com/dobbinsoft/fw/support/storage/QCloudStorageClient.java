package com.dobbinsoft.fw.support.storage;

import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

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
        cosClient.putObject(putObjectRequest);
        StorageResult result = new StorageResult();
        result.setSuc(true);
        result.setUrl(properties.getQcloudBaseUrl() + "/" + request.getPath() + "/" + request.getFilename());
        return result;
    }

    @Override
    public StoragePrivateResult savePrivate(StorageRequest request) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(request.getSize());
        objectMetadata.setContentType(request.getContentType());
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                properties.getQcloudBucket(),
                request.getPath() + "/" + request.getFilename(),
                request.getIs(), objectMetadata);
        cosClient.putObject(putObjectRequest);
        String key = request.getPath() + "/" + request.getFilename();
        cosClient.setObjectAcl(this.properties.getQcloudBucket(), key, CannedAccessControlList.Private);
        StoragePrivateResult result = new StoragePrivateResult();
        result.setSuc(true);
        result.setKey(key);
        result.setUrl(this.getPrivateUrl(key, 120));
        return result;
    }

    @Override
    public boolean delete(String url) {
        int index = url.indexOf("/", 5);
        String key = url.substring(index);
        cosClient.deleteObject(properties.getQcloudBucket(), key);
        return true;
    }

    @Override
    public boolean deletePrivate(String key) {
        cosClient.deleteObject(properties.getQcloudBucket(), key);
        return true;
    }

    @Override
    public String getPrivateUrl(String key, Integer expireSec) {
        return cosClient.generatePresignedUrl(properties.getQcloudBucket(), key, new Date(System.currentTimeMillis() + (1000L * expireSec))).toString();
    }

    @Override
    public String getKeyFormUrl(String url) {
        if (url.startsWith("http")) {
            url = url.replace("http://", "").replace("https://", "");
            int index = url.indexOf("/");
            String substring = url.substring(index + 1);
            int endIndex = substring.indexOf("?");
            if (endIndex > 0) {
                return substring.substring(0, endIndex);
            } else {
                return substring;
            }
        } else {
            return url;
        }
    }

}
