package com.dobbinsoft.fw.support.broadcast;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.sse.SSEBroadcastShareListener;
import com.dobbinsoft.fw.support.sse.SSEShareDTO;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.ws.WsBroadcastShareListener;
import com.dobbinsoft.fw.support.ws.WsShareDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BroadcastRedisReceiver {

    @Autowired(required = false)
    private BroadcastListener broadcastListener;

    @Autowired(required = false)
    private SSEBroadcastShareListener sseBroadcastShareListener;

    @Autowired(required = false)
    private WsBroadcastShareListener wsBroadcastShareListener;

    public void onMessage(String data, String channel) {
        BroadcastEvent broadcastEvent = JacksonUtil.parseObject(data, BroadcastEvent.class);
        assert broadcastEvent != null;
        if (broadcastEvent.getEvent().equals(Const.BROADCAST_CHANNEL_EVENT_SSE_SHARE)) {
            // event占用SSE信息共享发布
            SSEShareDTO sseShareDTO = JacksonUtil.parseObject(broadcastEvent.getMessage(), SSEShareDTO.class);
            sseBroadcastShareListener.onShare(sseShareDTO);
        } else if (broadcastEvent.getEvent().equals(Const.BROADCAST_CHANNEL_EVENT_WS_SHARE)) {
            // event占用SSE信息共享发布
            WsShareDTO wsShareDTO = JacksonUtil.parseObject(broadcastEvent.getMessage(), WsShareDTO.class);
            wsBroadcastShareListener.onShare(wsShareDTO);
        } else if (this.broadcastListener != null) {
            broadcastListener.onMessage(broadcastEvent.getEvent(), broadcastEvent.getMessage());
        } else {
            log.info("[Broadcast] 请实现 BroadcastListener 并放入IOC中以接收广播 message: {}", broadcastEvent.getMessage());
        }
    }

}
