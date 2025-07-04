package com.dobbinsoft.fw.support.component;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.dobbinsoft.fw.support.utils.JacksonUtil;

@Component
public class QueueComponent {

    @Autowired
    private BeforeGetCacheKey beforeGetCacheKey;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 入队（右侧入队）
     * @param queueName 队列名
     * @param value 入队的值
     */
    public void enqueue(String queueName, String value) {
        stringRedisTemplate.opsForList().rightPush(getKey(queueName), value);
    }

    /**
     * 出队（左侧出队）
     * @param queueName 队列名
     * @return 出队的值，队列为空时返回null
     */
    public String dequeue(String queueName) {
        return stringRedisTemplate.opsForList().leftPop(getKey(queueName));
    }

    /**
     * 获取队列长度
     * @param queueName 队列名
     * @return 队列长度
     */
    public long size(String queueName) {
        return stringRedisTemplate.opsForList().size(getKey(queueName));
    }

    /**
     * 查看队首元素但不移除
     * @param queueName 队列名
     * @return 队首元素，队列为空时返回null
     */
    public String peek(String queueName) {
        return stringRedisTemplate.opsForList().index(getKey(queueName), 0);
    }

    /**
     * 对象入队（自动序列化）
     * @param queueName 队列名
     * @param obj 入队对象
     */
    public void enqueueObj(String queueName, Object obj) {
        String json = JacksonUtil.toJSONString(obj);
        enqueue(queueName, json);
    }

    /**
     * 对象出队（Class反序列化）
     * @param queueName 队列名
     * @param clazz 反序列化类型
     * @return 出队对象
     */
    public <T> T dequeueObj(String queueName, Class<T> clazz) {
        String json = dequeue(queueName);
        if (json == null) {
            return null;
        }
        return JacksonUtil.parseObject(json, clazz);
    }

    /**
     * 对象出队（TypeReference泛型反序列化）
     * @param queueName 队列名
     * @param typeReference 反序列化类型
     * @return 出队对象
     */
    public <T> T dequeueObj(String queueName, TypeReference<T> typeReference) {
        String json = dequeue(queueName);
        if (json == null) {
            return null;
        }
        return JacksonUtil.parseObject(json, typeReference);
    }

    /**
     * 查看队首对象但不移除（Class反序列化）
     * @param queueName 队列名
     * @param clazz 反序列化类型
     * @return 队首对象
     */
    public <T> T peekObj(String queueName, Class<T> clazz) {
        String json = peek(queueName);
        if (json == null) {
            return null;
        }
        return JacksonUtil.parseObject(json, clazz);
    }

    /**
     * 查看队首对象但不移除（TypeReference泛型反序列化）
     * @param queueName 队列名
     * @param typeReference 反序列化类型
     * @return 队首对象
     */
    public <T> T peekObj(String queueName, TypeReference<T> typeReference) {
        String json = peek(queueName);
        if (json == null) {
            return null;
        }
        return JacksonUtil.parseObject(json, typeReference);
    }

    /**
     * 获取键 前置处理
     * @param key
     * @return
     */
    public String getKey(String key) {
        if (beforeGetCacheKey != null) {
            return beforeGetCacheKey.getKey(key);
        }
        return key;
    }

}
