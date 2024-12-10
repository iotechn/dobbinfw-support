package com.dobbinsoft.fw.support.broadcast;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class BroadcasterRedisImpl implements Broadcaster {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void publish(String event, String message) {
        BroadcastEvent broadcastEvent = new BroadcastEvent();
        broadcastEvent.setEvent(event);
        broadcastEvent.setMessage(message);
        stringRedisTemplate.convertAndSend(Const.BROADCAST_CHANNEL, JacksonUtil.toJSONString(broadcastEvent));
    }

}
