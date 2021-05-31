package com.dobbinsoft.fw.support.mq;

import java.io.Serializable;
import java.util.concurrent.FutureTask;

/**
 * ClassName: ReliableMessageHandler
 * Description: 可靠队列消息处理器
 *
 * @author: e-weichaozheng
 * @date: 2021-05-25
 */
public interface ReliableMessageHandler<T extends Serializable> {

    /**
     * @param t
     * @param bytes 直接过来的字节流
     * @return 返回true，表示Message确认，返回false，表示不确认。同时，true表示此次消息事务处理提交。
     */
    public boolean handle(T t, byte[] bytes);

    public String getTopic();

    public Class<T> getMessageClass();

}
