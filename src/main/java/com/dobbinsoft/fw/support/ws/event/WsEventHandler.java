package com.dobbinsoft.fw.support.ws.event;

public interface WsEventHandler<T extends WsEvent> {

    public String getEventType();

    public void handle(T t);

    public Class<T> clazz();

}
