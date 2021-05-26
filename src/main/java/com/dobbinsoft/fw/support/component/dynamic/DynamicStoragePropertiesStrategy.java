package com.dobbinsoft.fw.support.component.dynamic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 * ClassName: DynamicStoragePropertiesStrategy
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-05-26
 */
public class DynamicStoragePropertiesStrategy implements DynamicStorageStrategy {

    @Autowired
    private Environment environment;

    @Override
    public void write(String key, String value) {
        throw new RuntimeException("配置文件方式配置为静态配置，不可编辑");
    }

    @Override
    public String read(String key) {
        return environment.getProperty(key.replace(":", "."));
    }

    @Override
    public boolean del(String key) {
        throw new RuntimeException("配置文件方式配置为静态配置，不可编辑");
    }
}
