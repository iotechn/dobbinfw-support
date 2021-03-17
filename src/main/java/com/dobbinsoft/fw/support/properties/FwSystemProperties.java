package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * Description:
 * User: rize
 * Date: 2020/8/23
 * Time: 21:47
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.SYSTEM_CONFIG_PREFIX)
public class FwSystemProperties {

    /**
     * 用户会话周期（M）
     */
    private Integer userSessionPeriod;

    /**
     * 管理员会话周期（M）
     */
    private Integer adminSessionPeriod;

    /**
     * SSL 配置
     */
    private String sslCrtPath;

    private String sslKeyPath;

    private String guest;



}
