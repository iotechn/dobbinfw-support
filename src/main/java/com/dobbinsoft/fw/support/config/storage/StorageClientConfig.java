package com.dobbinsoft.fw.support.config.storage;

import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import com.dobbinsoft.fw.support.storage.AliStorageClient;
import com.dobbinsoft.fw.support.storage.MockStorageClient;
import com.dobbinsoft.fw.support.storage.QCloudStorageClient;
import com.dobbinsoft.fw.support.storage.StorageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by rize on 2019/4/26.
 */
@Configuration
public class StorageClientConfig {

    @Autowired
    private FwObjectStorageProperties properties;

    @Bean
    public StorageClient storageClient() {
        if ("qcloud".equals(properties.getEnable())) {
            return new QCloudStorageClient();
        } else if ("aliyun".equals(properties.getEnable())) {
            return new AliStorageClient();
        } else if ("mock".equals(properties.getEnable())) {
            return new MockStorageClient();
        } else {
            return new MockStorageClient();
        }
    }

}
