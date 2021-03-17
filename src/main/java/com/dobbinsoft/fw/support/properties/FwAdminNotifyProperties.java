package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * Description: 管理员统一推送动态配置
 * User: rize
 * Date: 2020/8/24
 * Time: 21:14
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.ADMIN_NOTIFY_CONFIG_PREFIX)
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
