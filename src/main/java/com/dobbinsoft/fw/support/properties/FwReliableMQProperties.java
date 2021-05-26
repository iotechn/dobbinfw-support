package com.dobbinsoft.fw.support.properties;

import com.dobbinsoft.fw.support.annotation.DynamicConfigProperties;
import lombok.Data;

/**
 * ClassName: FwRabbitMQProperties
 * Description: Rabbit MQ 动态配置信息
 *
 * @author: e-weichaozheng
 * @date: 2021-05-26
 */
@Data
@DynamicConfigProperties(prefix = FwDynamicConst.RELIABLE_MQ_CONFIG_PREFIX)
public class FwReliableMQProperties {

    /**
     * rabbit、
     */
    private String enable;

    private String rabbitHost;

    private Integer rabbitPort;

    private String rabbitVirtualHost;

    private String rabbitUsername;

    private String rabbitPassword;

    private String rabbitExchangeName;

    private Boolean rabbitAutoAck;


}
