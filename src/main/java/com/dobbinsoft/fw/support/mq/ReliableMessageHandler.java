package com.dobbinsoft.fw.support.mq;

import java.io.Serializable;

/**
 * ClassName: ReliableMessageHandler
 * Description: 可靠队列消息处理器
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public interface ReliableMessageHandler<T extends Serializable> {

    public void handle(T t);

    public String getTopic();

}
