package com.dobbinsoft.fw.support.ws;

import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

public class WsBroadcastShareListener {

    @Autowired
    private WsPublisher wsPublisher;

    public Mono<Void> onShare(WsShareDTO wsShareDTO) {
        return wsPublisher.send(wsShareDTO);
    }

}
