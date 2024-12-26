package com.dobbinsoft.fw.support.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * ClassName: MockStorageClient
 * Description: 模拟存储实现
 */
public class MockStorageClient implements StorageClient {

    private static final Logger logger = LoggerFactory.getLogger(MockStorageClient.class);

    @Override
    public StorageInfoResult info(String key) {
        // 获取对象的基本信息
        StorageInfoResult result = new StorageInfoResult();
        result.setExist(Boolean.TRUE);
        result.setKey(key);
        result.setContentType("image/png");
        result.setContentLength(1024 * 10L);
        return result;
    }

    @Override
    public StorageResult save(StorageRequest request) {
        logger.info("[模拟文件保存] ---------" + request.getFilename());
        StorageResult result = new StorageResult();
        result.setSuc(false);
        return result;
    }

    @Override
    public StoragePrivateResult savePrivate(StorageRequest request) {
        logger.info("[模拟文件保存] ---------" + request.getFilename());
        StoragePrivateResult result = new StoragePrivateResult();
        result.setSuc(false);
        return result;
    }

    @Override
    public boolean delete(String url) {
        return true;
    }

    @Override
    public boolean deletePrivate(String key) {
        return true;
    }

    @Override
    public String appendStyleForKey(String key, String style) {
        return key;
    }

    @Override
    public String appendVideoStyleForKey(String key, String style) {
        return key;
    }

    @Override
    public String getPrivateUrl(String key, Integer expireSec) {
        throw new RuntimeException("不支持私有保存");
    }

    @Override
    public String getKeyFormUrl(String url) {
        throw new RuntimeException("不支持私有保存");
    }

    @Override
    public boolean delPath(String path) {
        throw new RuntimeException("不支持批量删除");
    }

    @Override
    public StorageListResult listKeys(StorageListRequest request) {
        throw new RuntimeException("不支持列取文件");
    }

    @Override
    public String getPresignedUrl(String key, String method, Integer expireSec) {
        return null;
    }

    @Override
    public PresignedPostResult getPresignedUrlPost(String objectKey, Integer expireSec) {
        return null;
    }

    @Override
    public InputStream download(String key) {
        return null;
    }
}
