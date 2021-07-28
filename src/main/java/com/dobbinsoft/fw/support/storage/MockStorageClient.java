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
    public boolean delete(String url) {
        return true;
    }
}
