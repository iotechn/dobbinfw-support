package com.dobbinsoft.fw.support.mq;

import java.io.Serializable;

/**
 * ClassName: PulsarReliableMessageQueue
 * Description: Pulsar可靠队列
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public class PulsarReliableMessageQueue implements ReliableMessageQueue {

    @Override
    public boolean publish(String topic, Serializable message) {

        return false;
    }

}
