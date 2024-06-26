package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Description:
 * User: rize
 * Date: 2020/8/23
 * Time: 21:47
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.system")
public class FwSystemProperties {

    /**
     * 用户会话周期（S）
     */
    private Integer userSessionPeriod;

    /**
     * 管理员会话周期（S）
     */
    private Integer adminSessionPeriod;

    /**
     * 是否开启登录互斥
     */
    private Boolean mutexLogin;




}
