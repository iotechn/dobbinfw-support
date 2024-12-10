package com.dobbinsoft.fw.support.sse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SSEBroadcastShareListener {

    @Autowired
    private SSEPublisher ssePublisher;

    public void onShare(SSEShareDTO sseShareDTO) {
        ssePublisher.send(sseShareDTO);
    }

}
