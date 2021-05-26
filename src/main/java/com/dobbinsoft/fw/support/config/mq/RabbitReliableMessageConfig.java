package com.dobbinsoft.fw.support.config.mq;

import com.dobbinsoft.fw.support.properties.FwReliableMQProperties;
import com.rabbitmq.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: RabbitReliableMessageConfig
 * Description: RabbitMQ可靠队列配置
 *
 * @author: e-weichaozheng
 * @date: 2021-05-26
 */
public class RabbitReliableMessageConfig {

    @Autowired
    private FwReliableMQProperties fwReliableMQProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(fwReliableMQProperties.getRabbitHost());
        factory.setPort(fwReliableMQProperties.getRabbitPort());
        factory.setVirtualHost(fwReliableMQProperties.getRabbitVirtualHost());
        factory.setUsername(fwReliableMQProperties.getRabbitUsername());
        factory.setPassword(fwReliableMQProperties.getRabbitPassword());
        return factory;
    }

}
