package com.dobbinsoft.fw.support.storage;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.comm.ResponseMessage;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ClassName: AliStorageClient
 * Description: 阿里云对象存储实现
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public class AliStorageClient implements StorageClient, InitializingBean {

    @Autowired
    private FwObjectStorageProperties properties;

    private OSSClient ossClient;

    @Override
    public void afterPropertiesSet() throws Exception {
         ossClient = new OSSClient(properties.getAliEndpoint(), properties.getAliAccessKeyId(), properties.getAliAccessKeySecret());
    }

    @Override
    public StorageResult save(StorageRequest request) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(request.getSize());
        objectMetadata.setContentType(request.getContentType());
        PutObjectRequest putObjectRequest =
                new PutObjectRequest(
                        properties.getAliBucket(),
                        request.getPath() + "/" + request.getFilename(), request.getIs(), objectMetadata);
        putObjectRequest.setProcess("");
        PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
        ResponseMessage response = putObjectResult.getResponse();
        StorageResult result = new StorageResult();
        int statusCode = response.getStatusCode();
        result.setSuc(statusCode == 200);
        if (result.isSuc()) {
            result.setUrl(properties.getAliBaseUrl() + request.getPath() + "/" + request.getFilename());
        }
        return result;
    }

    @Override
    public boolean delete(String url) {
        int index = url.indexOf("/", 5);
        String key = url.substring(index);
        ossClient.deleteObject(properties.getAliBucket(), key);
        return true;
    }
}
