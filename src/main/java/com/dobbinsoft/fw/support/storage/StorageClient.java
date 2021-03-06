package com.dobbinsoft.fw.support.storage;

/**
 * ClassName: StorageClient
 * Description: 一个抽象的，存储接口
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public interface StorageClient {

    /**
     * 保存对象
     * @param request
     * @return
     */
    public StorageResult save(StorageRequest request);

    /**
     * 以私有的方式保存对象
     * @param request
     * @return
     */
    public StoragePrivateResult savePrivate(StorageRequest request);

    /**
     * 删除对象
     * @param url
     * @return
     */
    public boolean delete(String url);

    /**
     * 删除私有对象
     * @param key
     * @return
     */
    public boolean deletePrivate(String key);

    /**
     * 获取私有对象临时访问URL
     * @param key
     * @param expireSec
     * @return
     */
    public String getPrivateUrl(String key, Integer expireSec);

    /**
     * 通过URL获取文件Key
     * @param url
     * @return
     */
    public String getKeyFormUrl(String url);

}
