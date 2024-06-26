package com.dobbinsoft.fw.support.storage;

import java.io.InputStream;

/**
 * ClassName: StorageClient
 * Description: 一个抽象的，存储接口
 */
public interface StorageClient {

    /**
     *
     * @param key
     * @return
     */
    public StorageInfoResult info(String key);

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
     * 为 key 或公开的URL拼接样式
     * @param key
     * @param style
     */
    public String appendStyleForKey(String key, String style);

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

    /**
     * 删除path
     * @param path
     * @return
     */
    public boolean delPath(String path);

    /**
     * 获取文件
     * @param request
     * @return
     */
    public StorageListResult listKeys(StorageListRequest request);

    /**
     * 获取预签名，可以使流量不通过应用服务器而上传文件
     * @param key
     * @param method
     *     GET,
     *     POST,
     *     PUT,
     *     DELETE,
     *     HEAD,
     *     PATCH;
     * @param expireSec
     * @return
     */
    public String getPresignedUrl(String key, String method, Integer expireSec);


    /**
     * 获取预签名POST， 因为小程序不支持PUT上传文件
     * @param objectKey
     * @param expireSec
     * @return
     */
    public PresignedPostResult getPresignedUrlPost(String objectKey, Integer expireSec);

    /**
     * 下载文件到流，要自己关
     * @param key
     * @return
     */
    public InputStream download(String key);

}
