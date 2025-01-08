package com.dobbinsoft.fw.support.ws.event;

import com.dobbinsoft.fw.support.mq.DelayedMessageHandler;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class WsEventReceiver implements ApplicationContextAware {

    private Map<String, WsEventHandler<WsEvent>> handlerMap;

    public void route(String eventType, String json) {
        WsEventHandler<WsEvent> wsEventHandler = handlerMap.get(eventType);
        if (wsEventHandler == null) {
            log.info("[WS] 未找到事件处理器 eventType={}, json={}", eventType, json);
            return;
        }
        WsEvent o = JacksonUtil.parseObject(json, wsEventHandler.clazz());
        wsEventHandler.handle(o);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.handlerMap = (HashMap<String, WsEventHandler<WsEvent>>) applicationContext.getBean("wsMessageHandleRouter");
    }
}
