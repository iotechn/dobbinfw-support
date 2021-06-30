package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description: 管理员统一推送动态配置
 * User: rize
 * Date: 2020/8/24
 * Time: 21:14
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.ADMIN_NOTIFY_CONFIG_PREFIX)
@ConfigurationProperties(prefix = "com.dobbinsoft.admin-notify")
public class FwAdminNotifyProperties {

    private String enable;

    /**
     * Uninotify 服务器地址
     */
    private String uniNotifyUrl;

    /**
     * Uninotify AppId
     */
    private String uniNotifyAppId;

    /**
     * Uninotify AppSecret
     */
    private String uniNotifyAppSecret;

}
