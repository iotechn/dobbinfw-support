package com.dobbinsoft.fw.support.broadcast;

/**
 * 发布一条消息，本系统内所有节点收到消息
 * 注意：Redis实现 1.并不可靠，机器重启时，无法收到发布订阅消息。 2.
 */
public interface Broadcaster {

    public void publish(String event, String message);

}
