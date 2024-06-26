package com.dobbinsoft.fw.support.config.jackson;

import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonUtil.objectMapper;
    }

}
