package com.dobbinsoft.fw.support.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClassName: MockStorageClient
 * Description: MockStorageClient
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public class MockStorageClient implements StorageClient {

    private static final Logger logger = LoggerFactory.getLogger(MockStorageClient.class);

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
    public String getPrivateUrl(String key, Integer expireSec) {
        throw new RuntimeException("不支持私有保存");
    }

    @Override
    public String getKeyFormUrl(String url) {
        throw new RuntimeException("不支持私有保存");
    }
}
