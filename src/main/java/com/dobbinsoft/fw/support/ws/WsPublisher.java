package com.dobbinsoft.fw.support.ws;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.broadcast.Broadcaster;
import com.dobbinsoft.fw.support.model.WsWrapper;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.ws.event.WsEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Slf4j
public class WsPublisher {

    @Autowired(required = false)
    private Broadcaster broadcaster;

    // 用户身份的唯一key
    private final Map<String, WebSocketSession> clients = new ConcurrentHashMap<>();

    public void join(String identityOwnerKey, WebSocketSession webSocketSession) {
        WebSocketSession existSession = clients.get(identityOwnerKey);
        if (existSession != null) {
            try {
                existSession.close();
            } catch (IOException e) {
                log.info("[WS] {} - {}强迫下线，已经其他设备登录", identityOwnerKey, existSession.getId());
            }
        }
        clients.put(identityOwnerKey, webSocketSession);
    }

    /**
     * 发送一条SSE消息
     * @param identityOwnerKey
     * @param event
     */
    public void send(String identityOwnerKey, WsEvent event) {
        try {
            WebSocketSession webSocketSession = clients.get(identityOwnerKey);
            String textMessage = JacksonUtil.toJSONString(event);
            if (webSocketSession != null) {
                webSocketSession.sendMessage(new TextMessage(textMessage));
                log.info("[WS] sendTo: {} with message: {}", identityOwnerKey, textMessage);
            } else if (broadcaster != null){
                WsShareDTO wsShareDTO = new WsShareDTO();
                wsShareDTO.setIdentityOwnerKey(identityOwnerKey);
                wsShareDTO.setEventJson(textMessage);
                broadcaster.publish(Const.BROADCAST_CHANNEL_EVENT_WS_SHARE, JacksonUtil.toJSONString(wsShareDTO));
            } else {
                log.info("[WS] 目标客户端不在线: {}", identityOwnerKey);
            }
        } catch (IOException e) {
            log.error("[WS] 网络错误{}", e.getMessage());
        }
    }

    public void quit(String identityOwnerKey) {
        WebSocketSession existSession = clients.remove(identityOwnerKey);
        if (existSession != null) {
            try {
                existSession.close();
            } catch (IOException e) {
                log.info("[WS] {} - {} 主动断开连接", identityOwnerKey, existSession.getId());
            }
        }
    }

    void send(WsShareDTO wsShareDTO) {
        try {
            WebSocketSession webSocketSession = clients.get(wsShareDTO.getIdentityOwnerKey());
            if (webSocketSession != null) {
                webSocketSession.sendMessage(new TextMessage( wsShareDTO.getEventJson()));
                log.info("[WS] sendTo: {} with message: {}", wsShareDTO.getIdentityOwnerKey(), wsShareDTO.getEventJson());
            } else {
                log.info("[WS] 目标客户端不在线或不在本机: {}", wsShareDTO.getIdentityOwnerKey());
            }
        } catch (IOException e) {
            log.error("[WS] 网络错误{}", e.getMessage());
        }
    }

}
