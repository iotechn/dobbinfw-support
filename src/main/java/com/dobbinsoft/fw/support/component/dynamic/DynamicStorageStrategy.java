package com.dobbinsoft.fw.support.component.dynamic;

/**
 * ClassName: DynamicStorageStrategy
 * Description: 动态配置持久化策略
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
public interface DynamicStorageStrategy {

    /**
     * 覆盖写，若存在，则更新，若不存在则写
     * @param key
     * @param value
     */
    public void write(String key, String value);

    public String read(String key);

    public boolean del(String key);

}
