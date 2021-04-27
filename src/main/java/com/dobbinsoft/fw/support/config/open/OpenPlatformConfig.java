package com.dobbinsoft.fw.support.config.open;

import com.dobbinsoft.fw.support.component.open.OpenPlatform;
import com.dobbinsoft.fw.support.quartz.OPNotifyQuartz;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: OpenPlatformConfig
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
public class OpenPlatformConfig {

    @Bean
    public OpenPlatform openPlatform() {
        return new OpenPlatform();
    }

    @Bean
    public OPNotifyQuartz opNotifyQuartz() {
        return new OPNotifyQuartz();
    }

}
