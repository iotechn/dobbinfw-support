package com.dobbinsoft.fw.support.config.mq;

import com.dobbinsoft.fw.support.mq.PulsarReliableMessageQueue;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.context.annotation.Bean;

/**
 * ClassName: PulsarReliableMessageConfig
 * Description: Pulsar 可靠队列配置
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public class PulsarReliableMessageConfig {

    @Bean
    public PulsarClient pulsarClient() {
        PulsarClient client = null;
        try {
            client = PulsarClient.builder()
                    .serviceUrl("pulsar://*.*.*.*:6000/")
                    .listenerName("custom:1********0/vpc-******/subnet-********")//custom:+路由ID
                    .authentication(AuthenticationFactory.token("eyJh****"))
                    .build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
        return client;
    }

    @Bean
    public PulsarReliableMessageQueue pulsarReliableMessageQueue() {
        return new PulsarReliableMessageQueue();
    }

}
