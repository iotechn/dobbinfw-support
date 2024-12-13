package com.dobbinsoft.fw.support.sse;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.broadcast.Broadcaster;
import com.dobbinsoft.fw.support.model.SseEmitterWrapper;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端消息发布器
 * 一个已登录的人员（任意身份），可以发起一个SSE请求，连接到服务器
 * 服务器有事件产生，通过ServerEventPublisher发布
 *
 * 本类需要解决的问题
 * 1. Event可能在任意一个节点上产生。但是SSE请求在特定的服务器上。 --- 这里需要可靠的广播机制
 * 2. 生命周期，2.1. 用户登录直到退出程序。 2.2. 一次会话，例如GPT的一次请求。
 */
@Slf4j
@Component
public class SSEPublisher {

    @Autowired
    private Broadcaster broadcaster;

    // 用户身份的唯一key
    private final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    /**
     * 发送一条SSE消息
     * @param identityOwnerKey
     * @param message
     */
    public void send(String identityOwnerKey, String message) {
        try {
            SseEmitter sseEmitter = clients.get(identityOwnerKey);
            if (sseEmitter != null) {
                sseEmitter.send(message);
                log.info("[SSE] sendTo: {} with message: {}", identityOwnerKey, message);
            } else {
                SSEShareDTO sseShareDTO = new SSEShareDTO();
                sseShareDTO.setIdentityOwnerKey(identityOwnerKey);
                sseShareDTO.setMessage(message);
                broadcaster.publish(Const.BROADCAST_CHANNEL_EVENT_SSE_SHARE, JacksonUtil.toJSONString(sseShareDTO));
            }
        } catch (IOException e) {
            log.error("[SSE] 网络错误{}", e.getMessage());
        }
    }

    void send(SSEShareDTO sseShareDTO) {
        try {
            SseEmitter sseEmitter = clients.get(sseShareDTO.getIdentityOwnerKey());
            if (sseEmitter != null) {
                sseEmitter.send(sseShareDTO.getMessage());
                log.info("[SSE] sendTo: {} with message: {}", sseShareDTO.getIdentityOwnerKey(), sseShareDTO.getMessage());
            } else {
                log.info("[SSE] 目标客户端不在线或不在本机: {}", sseShareDTO.getIdentityOwnerKey());
            }
        } catch (IOException e) {
            log.error("[SSE] 网络错误{}", e.getMessage());
        }
    }


    /**
     *
     * @param identityOwnerKey
     * @return
     */
    public SseEmitterWrapper join(String identityOwnerKey) {
        return join(identityOwnerKey, new SseEmitter(0L));
    }

    /**
     *
     * @param identityOwnerKey
     * @param sseEmitter
     */
    public SseEmitterWrapper join(String identityOwnerKey, SseEmitter sseEmitter) {
        sseEmitter.onCompletion(() -> {
            log.info("[SSE] 完成通信 identityOwnerKey:{}", identityOwnerKey);
            clients.remove(identityOwnerKey);
        });

        sseEmitter.onError((e) -> {
            log.error("[SSE] 异常 identityOwnerKey:{}", identityOwnerKey, e);
            clients.remove(identityOwnerKey);
        });

        sseEmitter.onTimeout(() -> {
            log.info("[SSE] 通信超时 identityOwnerKey:{}", identityOwnerKey);
            clients.remove(identityOwnerKey);
        });

        clients.put(identityOwnerKey, sseEmitter);
        return SseEmitterWrapper.build(identityOwnerKey, sseEmitter);
    }

}
