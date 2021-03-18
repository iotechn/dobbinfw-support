package com.dobbinsoft.fw.support.mq;

/**
 * 延迟消息处理器
 */
public interface DelayedMessageHandler {

    /**
     *
     * @param value
     * @return 处理成功的返回大于0结果,失败返回0
     */
    public int handle(String value);

    public int getCode();
}
