package com.dobbinsoft.fw.support.ws;

import org.springframework.beans.factory.annotation.Autowired;

public class WsBroadcastShareListener {

    @Autowired
    private WsPublisher wsPublisher;

    public void onShare(WsShareDTO wsShareDTO) {
        wsPublisher.send(wsShareDTO);
    }

}
