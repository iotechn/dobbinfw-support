package com.dobbinsoft.fw.support.session;

import com.dobbinsoft.fw.core.entiy.inter.IdentityOwner;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.ObjectUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;


public class SessionStorageRedisImpl implements SessionStorage {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private boolean mutex = false;

    public SessionStorageRedisImpl() {}

    public SessionStorageRedisImpl(boolean mutex) {
        this.mutex = mutex;
    }

    @Override
    public void save(String prefix, String token, IdentityOwner identityOwner, Integer expire) {
        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("Token不能为空");
        }
        stringRedisTemplate.opsForValue().set(prefix + token, identityOwner.getId().toString(), expire, TimeUnit.SECONDS);
        SessionWrapper wrapper = new SessionWrapper();
        wrapper.setIdentityOwnerJson(JacksonUtil.toJSONString(identityOwner));
        wrapper.setToken(token);
        stringRedisTemplate.opsForHash().put(prefix, identityOwner.getId().toString(), JacksonUtil.toJSONString(wrapper));
    }

    @Override
    public void refresh(String prefix, IdentityOwner identityOwner) {
        Object o = stringRedisTemplate.opsForHash().get(prefix, identityOwner.getId().toString());
        if (ObjectUtils.isNotEmpty(o)) {
            SessionWrapper wrapper = JacksonUtil.parseObject(o.toString(), SessionWrapper.class);
            assert wrapper != null;
            wrapper.setIdentityOwnerJson(JacksonUtil.toJSONString(identityOwner));
            stringRedisTemplate.opsForHash().put(prefix, identityOwner.getId().toString(), JacksonUtil.toJSONString(wrapper));
        }
    }

    @Override
    public <T extends IdentityOwner> T get(String prefix, String token, Class<T> clazz) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String idStr = stringRedisTemplate.opsForValue().get(prefix + token);
        if (StringUtils.isEmpty(idStr)) {
            return null;
        }
        Object o = stringRedisTemplate.opsForHash().get(prefix, idStr);
        if (o == null) {
            // 说明已经登出全部了
            stringRedisTemplate.delete(prefix + token);
            return null;
        }
        SessionWrapper wrapper = JacksonUtil.parseObject(o.toString(), SessionWrapper.class);
        if (wrapper == null) {
            return null;
        }
        String identityOwnerJson = wrapper.getIdentityOwnerJson();
        T t = JacksonUtil.parseObject(identityOwnerJson, clazz);
        if (this.mutex) {
            if (!StringUtils.equals(wrapper.getToken(), token)) {
                // 互斥
                stringRedisTemplate.delete(prefix + token);
                return null;
            }
        }

        return t;
    }

    @Override
    public <T extends IdentityOwner> T get(String prefix, Long id, Class<T> clazz) {
        Object o = stringRedisTemplate.opsForHash().get(prefix, id.toString());
        if (o == null) {
            return null;
        }
        SessionWrapper wrapper = JacksonUtil.parseObject(o.toString(), SessionWrapper.class);
        if (wrapper == null) {
            return null;
        }
        String identityOwnerJson = wrapper.getIdentityOwnerJson();
        return JacksonUtil.parseObject(identityOwnerJson, clazz);
    }

    @Override
    public void renew(String prefix, String token, Integer expire) {
        if (StringUtils.isEmpty(token)) {
            return;
        }
        stringRedisTemplate.expire(prefix + token, expire, TimeUnit.SECONDS);
    }

    @Override
    public void logout(String prefix, String token) {
        stringRedisTemplate.delete(prefix + token);
    }

    @Override
    public void logoutAll(String prefix, Long id) {
        assert id != null;
        stringRedisTemplate.opsForHash().delete(prefix, id.toString());
    }
}
