package com.dobbinsoft.fw.support.mq;

import java.io.Serializable;

/**
 * ClassName: ReliableMessageQueue
 * Description: 可靠的消息队列
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public interface ReliableMessageQueue {

    public boolean publish(String topic, Serializable message);

}
