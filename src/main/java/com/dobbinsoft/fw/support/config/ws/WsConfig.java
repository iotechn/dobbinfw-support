package com.dobbinsoft.fw.support.config.ws;

import com.dobbinsoft.fw.support.broadcast.Broadcaster;
import com.dobbinsoft.fw.support.ws.WsBroadcastShareListener;
import com.dobbinsoft.fw.support.ws.WsPublisher;
import com.dobbinsoft.fw.support.ws.event.WsEventHandler;
import com.dobbinsoft.fw.support.ws.event.WsEventReceiver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WsConfig {

    @Bean
    @ConditionalOnBean(Broadcaster.class)
    public WsBroadcastShareListener wsBroadcastShareListener() {
        return new WsBroadcastShareListener();
    }

    @Bean
    public WsPublisher wsPublisher() {
        return new WsPublisher();
    }

    @Bean
    public Map<String, WsEventHandler> wsMessageHandleRouter(List<WsEventHandler> wsEventHandlerList) {
        return wsEventHandlerList.stream().collect(Collectors.toMap(WsEventHandler::getEventType, v -> v));
    }

    @Bean
    public WsEventReceiver wsEventReceiver() {
        return new WsEventReceiver();
    }


}
