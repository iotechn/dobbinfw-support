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

}
