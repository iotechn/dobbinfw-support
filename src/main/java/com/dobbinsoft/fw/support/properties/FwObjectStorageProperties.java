package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description:
 * User: rize
 * Date: 2020/8/6
 * Time: 15:42
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.OSS_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.oss")
public class FwObjectStorageProperties {

    private String enable;

    private String aliAccessKeyId;

    private String aliAccessKeySecret;

    private String aliEndpoint;

    private String aliBucket;

    /**
     * 启用CDN的情况基础URL
     */
    private String aliBaseUrl;

    private String qcloudSecretId;

    private String qcloudSecretKey;

    private String qcloudRegion;

    private String qcloudBucket;

    /**
     * 启用CDN的情况基础URL
     */
    private String qcloudBaseUrl;

}
