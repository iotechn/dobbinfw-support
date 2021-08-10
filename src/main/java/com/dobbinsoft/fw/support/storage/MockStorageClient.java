package com.dobbinsoft.fw.support.storage;

/**
 * ClassName: MockStorageClient
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public class MockStorageClient implements StorageClient{

    @Override
    public StorageResult save(StorageRequest request) {
        StorageResult result = new StorageResult();
        result.setSuc(false);
        return result;
    }

    @Override
    public StoragePrivateResult savePrivate(StorageRequest request) {
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
