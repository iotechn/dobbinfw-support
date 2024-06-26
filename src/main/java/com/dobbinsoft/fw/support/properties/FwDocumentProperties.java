package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.document")
public class FwDocumentProperties {

    private String modelScan;

}
