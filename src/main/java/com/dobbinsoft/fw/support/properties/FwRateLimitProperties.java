package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FwRateLimitProperties
 * Description: 限流配置配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.rate-limit")
public class FwRateLimitProperties {

    private String enable;

}
