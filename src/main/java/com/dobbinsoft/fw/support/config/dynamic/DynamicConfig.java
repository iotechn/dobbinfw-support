package com.dobbinsoft.fw.support.config.dynamic;

import com.dobbinsoft.fw.support.aspect.DynamicConfigAspect;
import com.dobbinsoft.fw.support.component.dynamic.DynamicConfigComponent;
import com.dobbinsoft.fw.support.component.dynamic.DynamicStorageFileSystemStrategy;
import com.dobbinsoft.fw.support.component.dynamic.DynamicStorageStrategy;
import com.dobbinsoft.fw.support.component.open.OpenPlatformStorageStrategy;
import com.dobbinsoft.fw.support.component.open.model.OPClient;
import com.dobbinsoft.fw.support.component.open.model.OPClientPermission;
import com.dobbinsoft.fw.support.component.open.model.OPNotify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ClassName: DynamicConfig
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-05-20
 */
@Configuration
public class DynamicConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean(DynamicStorageStrategy.class)
    public DynamicStorageStrategy dynamicStorageStrategy() {
        return new DynamicStorageFileSystemStrategy();
    }

    @Bean
    @ConditionalOnBean(DynamicStorageStrategy.class)
    public DynamicConfigComponent dynamicConfigComponent() {
        return new DynamicConfigComponent();
    }

    @Bean
    @ConditionalOnBean(DynamicStorageStrategy.class)
    public DynamicConfigAspect dynamicConfigAspect() {
        return new DynamicConfigAspect();
    }

}
