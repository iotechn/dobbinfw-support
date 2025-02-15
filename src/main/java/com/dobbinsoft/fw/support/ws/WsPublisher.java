package com.dobbinsoft.fw.support.ws;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.broadcast.Broadcaster;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.ws.event.WsEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Void> join(String identityOwnerKey, WebSocketSession webSocketSession) {
        WebSocketSession existSession = clients.get(identityOwnerKey);
        if (existSession != null) {
            return existSession.close();
        }
        clients.put(identityOwnerKey, webSocketSession);
        return Mono.empty();
    }

    /**
     * 发送一条SSE消息
     *
     * @param identityOwnerKey
     * @param event
     * @return
     */
    public Mono<Void> send(String identityOwnerKey, WsEvent event) {
        WebSocketSession webSocketSession = clients.get(identityOwnerKey);
        String textMessage = JacksonUtil.toJSONString(event);
        if (webSocketSession != null) {
            WebSocketMessage webSocketMessage = webSocketSession.textMessage(textMessage);
            Mono<Void> mono = webSocketSession.send(Flux.just(webSocketMessage));
            log.info("[WS] sendTo: {} with message: {}", identityOwnerKey, textMessage);
            return mono;
        } else if (broadcaster != null){
            WsShareDTO wsShareDTO = new WsShareDTO();
            wsShareDTO.setIdentityOwnerKey(identityOwnerKey);
            wsShareDTO.setEventJson(textMessage);
            broadcaster.publish(Const.BROADCAST_CHANNEL_EVENT_WS_SHARE, JacksonUtil.toJSONString(wsShareDTO));
        } else {
            log.info("[WS] 目标客户端不在线: {}", identityOwnerKey);
        }
        return Mono.empty();
    }

    public Mono<Void> quit(String identityOwnerKey) {
        WebSocketSession existSession = clients.remove(identityOwnerKey);
        if (existSession != null) {
            return existSession.close();
        }
        return Mono.empty();
    }

    Mono<Void> send(WsShareDTO wsShareDTO) {
        WebSocketSession webSocketSession = clients.get(wsShareDTO.getIdentityOwnerKey());
        if (webSocketSession != null) {
            WebSocketMessage webSocketMessage = webSocketSession.textMessage(wsShareDTO.getEventJson());
            Mono<Void> mono = webSocketSession.send(Flux.just(webSocketMessage));
            log.info("[WS] sendTo: {} with message: {}", wsShareDTO.getIdentityOwnerKey(), wsShareDTO.getEventJson());
            return mono;
        } else {
            log.info("[WS] 目标客户端不在线或不在本机: {}", wsShareDTO.getIdentityOwnerKey());
        }
        return Mono.empty();
    }

}
