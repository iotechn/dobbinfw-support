package com.dobbinsoft.fw.support.storage;

/**
 * ClassName: StorageClient
 * Description: 一个抽象的，存储接口
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public interface StorageClient {

    public StorageResult save(StorageRequest request);

    public StoragePrivateResult savePrivate(StorageRequest request);

    public boolean delete(String url);

    public boolean deletePrivate(String key);

    public String getPrivateUrl(String key, Integer expireSec);

    public String getKeyFormUrl(String url);

}
