package com.dobbinsoft.fw.support.mq;

import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.properties.FwReliableMQProperties;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;

/**
 * ClassName: AbstractReliableMessageHandler
 * Description: 一个抽象的RabbitMQ，用于自动创建Consumer
 *
 * @author: e-weichaozheng
 * @date: 2021-05-26
 */
public abstract class AbstractReliableMessageHandler<T extends Serializable> implements ReliableMessageHandler<T>, InitializingBean {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private FwReliableMQProperties fwReliableMQProperties;

    private Connection connection;

    private static final Logger logger = LoggerFactory.getLogger(AbstractReliableMessageHandler.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        if ("rabbit".equals(fwReliableMQProperties.getEnable())) {
            this.connection = connectionFactory.newConnection();
            Channel channel = this.connection.createChannel();
            channel.exchangeDeclare(fwReliableMQProperties.getRabbitExchangeName(), "topic", false, false, null);
            channel.queueDeclare(getTopic(), false, false, false, null);
            // 绑定队列和交换机
            channel.queueBind(getTopic(), fwReliableMQProperties.getRabbitExchangeName(), "topic");
            // 创建消费者
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    Class<T> tClass = (Class) AbstractReliableMessageHandler.class.getGenericInterfaces()[0];
                    String msg = new String(body, "UTF-8");
                    // 反序列化为 T
                    T t = JSONObject.parseObject(msg, tClass);
                    AbstractReliableMessageHandler.this.handle(t);
                }
            };
            // 开始获取消息
            channel.basicConsume(getTopic(), fwReliableMQProperties.getRabbitAutoAck(), consumer);
            logger.info("[Rabbit MQ] 消费者开始监听消息 Topic=" + getTopic());
        } else {
            throw new RuntimeException("不支持队列类型：" + fwReliableMQProperties.getEnable());
        }
    }


}
