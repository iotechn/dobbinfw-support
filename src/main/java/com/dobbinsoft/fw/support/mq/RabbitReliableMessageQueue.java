package com.dobbinsoft.fw.support.mq;

import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.properties.FwReliableMQProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

/**
 * ClassName: RabbitReliableMessageQueue
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public class RabbitReliableMessageQueue implements ReliableMessageQueue {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private FwReliableMQProperties fwReliableMQProperties;

    private Channel channel;

    private static final Logger logger = LoggerFactory.getLogger(RabbitReliableMessageQueue.class);

    public RabbitReliableMessageQueue() throws IOException, TimeoutException {
        Connection conn = this.connectionFactory.newConnection();
        // 创建消息通道
        this.channel = conn.createChannel();
    }

    @Override
    public boolean publish(String topic, Serializable message) {
        try {
            channel.basicPublish(fwReliableMQProperties.getRabbitExchangeName(), "topic", null, JSONObject.toJSONBytes(message));
            return true;
        } catch (IOException e) {
            logger.error("[消息队列推送] IO异常", e);
            return false;
        }
    }

}
