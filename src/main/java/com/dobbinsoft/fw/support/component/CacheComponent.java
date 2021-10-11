package com.dobbinsoft.fw.support.component;

import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by rize on 2019/3/22.
 */
@Component
public class CacheComponent {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private BeforeGetCacheKey beforeGetCacheKey;

    /**
     * 用于描述一系列的 将要执行的 Redis （写）操作
     */
    private ThreadLocal<CacheContext> contextThreadLocal = new ThreadLocal<>();

    /**
     * 放入不过期不序列化缓存
     *
     * @param key
     * @param value
     */
    public void putRaw(String key, String value) {
        stringRedisTemplate.opsForValue().set(getKey(key), value);
    }

    /**
     * 放入不过期不序列化缓存
     *
     * @param key
     * @param value
     * @param expireSec
     */
    public void putRaw(String key, String value, Integer expireSec) {
        stringRedisTemplate.opsForValue().set(getKey(key), value, expireSec, TimeUnit.SECONDS);
    }

    /**
     * 直接获取不反序列化缓存
     *
     * @param key
     * @return
     */
    public String getRaw(String key) {
        return stringRedisTemplate.opsForValue().get(getKey(key));
    }


    /**
     * 放入对象/集合，进行序列化
     *
     * @param key
     * @param obj
     */
    public void putObj(String key, Object obj) {
        stringRedisTemplate.opsForValue().set(getKey(key), JSONObject.toJSONString(obj));
    }

    /**
     * 放入对象/集合，进行序列化，带过期时间
     *
     * @param key
     * @param obj
     * @param expireSec
     */
    public void putObj(String key, Object obj, Integer expireSec) {
        stringRedisTemplate.opsForValue().set(getKey(key), JSONObject.toJSONString(obj), expireSec, TimeUnit.SECONDS);
    }

    /**
     * 获取对象进行序列化
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getObj(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(getKey(key));
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return JSONObject.parseObject(json, clazz);
    }

    /**
     * 获取对象列表
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getObjList(String key, Class<T> clazz) {
        String json = stringRedisTemplate.opsForValue().get(getKey(key));
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return JSONObject.parseArray(json, clazz);
    }

    /**
     * 将值放入Hash里
     *
     * @param hashName
     * @param key
     * @param value
     */
    public void putHashRaw(String hashName, String key, String value) {
        stringRedisTemplate.opsForHash().put(hashName, getKey(key), value);
    }

    /**
     * 设置Hash对象，进行序列化
     *
     * @param hashName
     * @param key
     * @param obj
     */
    public void putHashObj(String hashName, String key, Object obj) {
        stringRedisTemplate.opsForHash().put(hashName, getKey(key), JSONObject.toJSONString(obj));
    }

    /**
     * 设置Hash对象，进行序列化
     *
     * @param hashName
     * @param key
     * @param obj
     * @param expireSec
     */
    public void putHashObj(String hashName, String key, Object obj, Integer expireSec) {
        String k = getKey(key);
        boolean hasKey = stringRedisTemplate.hasKey(k);
        stringRedisTemplate.opsForHash().put(hashName, k, JSONObject.toJSONString(obj));
        if (!hasKey) {
            stringRedisTemplate.expire(k, expireSec, TimeUnit.SECONDS);
        }
    }

    /**
     * 增加Hash表中键的字面数值
     *
     * @param hashName
     * @param key
     * @param delta
     * @return
     */
    public long incrementHashKey(String hashName, String key, long delta) {
        return stringRedisTemplate.opsForHash().increment(hashName, getKey(key), delta);
    }

    /**
     * 减少Hash表中字面的数值
     *
     * @param hashName
     * @param key
     * @param delta
     * @return
     */
    public long decrementHashKey(String hashName, String key, long delta) {
        return stringRedisTemplate.opsForHash().increment(hashName, getKey(key), -delta);
    }

    /**
     * 获取Hash值，不进行序列化
     *
     * @param hashName
     * @param key
     * @return
     */
    public String getHashRaw(String hashName, String key) {
        String o = (String) stringRedisTemplate.opsForHash().get(hashName, getKey(key));
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return o;
    }

    /**
     * 获取Hash值，带反序列化
     *
     * @param hashName
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getHashObj(String hashName, String key, Class<T> clazz) {
        String o = (String) stringRedisTemplate.opsForHash().get(hashName, getKey(key));
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseObject(o, clazz);
    }

    /**
     * 获取Hash值，以数组的形式反序列化
     *
     * @param hashName
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getHashList(String hashName, String key, Class<T> clazz) {
        String o = (String) stringRedisTemplate.opsForHash().get(hashName, getKey(key));
        if (StringUtils.isEmpty(o)) {
            return null;
        }
        return JSONObject.parseArray(o, clazz);
    }

    /**
     * 批量获取Hash表里面的值
     *
     * @param key 桶的名字
     * @param hashNameCollection String类型键集合 Collection<String>
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getHashMultiAsList(String key, Collection hashNameCollection, Class<T> clazz) {
        List<String> list = stringRedisTemplate.opsForHash().multiGet(getKey(key), hashNameCollection);
        return list.stream().map(item -> JSONObject.parseObject(item, clazz)).collect(Collectors.toList());
    }

    /**
     * 批量获取Hash值 列表
     *
     * @param key
     * @param hashNameCollection
     * @return
     */
    public List<String> getHashMultiAsRawList(String key, Collection hashNameCollection) {
        List<String> list = stringRedisTemplate.opsForHash().multiGet(getKey(key), hashNameCollection);
        return list;
    }

    /**
     * 删除Hash值
     *
     * @param hashName
     * @param key
     */
    public void delHashKey(String hashName, String key) {
        stringRedisTemplate.opsForHash().delete(hashName, getKey(key));
    }

    public void putHashAll(String key, Map<String, String> map, Integer expireSec) {
        String k = getKey(key);
        stringRedisTemplate.opsForHash().putAll(k, map);
        stringRedisTemplate.expire(k, expireSec, TimeUnit.SECONDS);
    }

    public Map<String,String> getHashAll(String key) {
        String k = getKey(key);
        if (!stringRedisTemplate.hasKey(k)) {
            return null;
        }
        return (Map)stringRedisTemplate.opsForHash().entries(k);
    }


    /**
     * 向有序集合中添加元素
     *
     * @param setName
     * @param source
     * @param value
     */
    public void putZSet(String setName, double source, String value) {
        stringRedisTemplate.opsForZSet().add(getKey(setName), value, source);
    }

    public void putZSetMulti(String setName, Set<ZSetOperations.TypedTuple<String>> values) {
        stringRedisTemplate.opsForZSet().add(getKey(setName), values);
    }

    /**
     * 从有序集合中移除数据
     *
     * @param setName
     * @param value
     */
    public void delZSet(String setName, String value) {
        stringRedisTemplate.opsForZSet().remove(getKey(setName), value);
    }

    /**
     * 从有序集合中分页获取数据
     *
     * @param setName
     * @param pageNo
     * @param pageSize
     * @param isAsc
     * @return
     */
    public Page<String> getZSetPage(String setName, int pageNo, int pageSize, boolean isAsc) {
        String key = getKey(setName);
        Long size = stringRedisTemplate.opsForZSet().size(key);
        List<String> list = new ArrayList<>();
        if (size > 0) {
            if (isAsc) {
                list.addAll(stringRedisTemplate.opsForZSet().range(key, (pageNo - 1) * pageSize, pageNo * pageSize - 1));
            } else {
                list.addAll(stringRedisTemplate.opsForZSet().reverseRange(key, (pageNo - 1) * pageSize, pageNo * pageSize - 1));
            }
        }
        return new Page<>(list, pageNo, pageSize, size);
    }

    /**
     * 从有序集合中获取数据
     *
     * @param setName
     * @param isAsc
     * @return
     */
    public Set<String> getZSetList(String setName, boolean isAsc) {
        String key = getKey(setName);
        Long size = stringRedisTemplate.opsForZSet().size(key);
        if (isAsc) {
            return stringRedisTemplate.opsForZSet().range(key, 0, size);
        } else {
            return stringRedisTemplate.opsForZSet().reverseRange(key, 0, size);
        }
    }

    /**
     * 设置Lru，最后进来的排最前面
     * @param setName
     * @param value
     * @param max
     * @param exceed 可允许超出范围，清理缓存区。
     */
    public void putZSetLru(String setName, String value, int max, int exceed) {
        String key = getKey(setName);
        Long size = stringRedisTemplate.opsForZSet().size(key);
        if (size > max + exceed - 1) {
            //超过了。淘汰了
            stringRedisTemplate.opsForZSet().removeRange(key, size - exceed, size);
        }
        stringRedisTemplate.opsForZSet().add(key, value, -System.currentTimeMillis());
    }

    /**
     * 增加ZSet分数
     * @param setName
     * @param value
     * @param delta
     */
    public Double incZSetSource(String setName, String value, double delta) {
        return stringRedisTemplate.opsForZSet().incrementScore(getKey(setName), value, delta);
    }

    /**
     * 获取前N个
     * @param setName
     * @param n
     * @return
     */
    public Set<String> getZSetLruTopN(String setName, int n) {
        return stringRedisTemplate.opsForZSet().range(getKey(setName), 0 , n);
    }


    /**
     * TODO 保证原子性问题
     * 向一个set中添加数据
     * @param key
     * @param member
     * @param expireSec
     */
    public void putSetRaw(String key, String member, Integer expireSec) {
        stringRedisTemplate.opsForSet().add(getKey(key), member);
        stringRedisTemplate.expire(getKey(key), expireSec, TimeUnit.SECONDS);
    }

    /**
     * TODO 保证原子性问题
     * @param key
     * @param set
     * @param expireSec
     */
    public void putSetRawAll(String key, String[] set, Integer expireSec) {
        stringRedisTemplate.opsForSet().add(getKey(key), set);
        stringRedisTemplate.expire(getKey(key), expireSec, TimeUnit.SECONDS);
    }

    public void removeSetRaw(String key, String member) {
        stringRedisTemplate.opsForSet().remove(getKey(key), member);
    }

    public boolean isSetMember(String key, String member) {
        return stringRedisTemplate.opsForSet().isMember(getKey(key), member);
    }


    /**
     * 删除键 / 桶 / hash 表等
     *
     * @param key
     */
    public void del(String key) {
        stringRedisTemplate.delete(getKey(key));
    }

    /**
     * 判断是否包含键
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        return stringRedisTemplate.hasKey(getKey(key));
    }


    /**
     * 获取指定前缀的Key
     *
     * @param prefix
     * @return
     */
    public Set<String> getPrefixKeySet(String prefix) {
        return stringRedisTemplate.keys(getKey(prefix) + "*");
    }

    public void delPrefixKey(String prefix) {
        Set<String> prefixKeySet = getPrefixKeySet(prefix);
        for (String key : prefixKeySet) {
            stringRedisTemplate.delete(key);
        }
    }

    /**
     * 获取redis中键的过期时间
     * 返回秒
     * @param key
     * @return
     */
    public Long getKeyExpire(String key){
        return stringRedisTemplate.getExpire(getKey(key));
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

    /**
     * 获取缓存上下文
     * @return
     */
    public CacheContext getCacheContext() {
        CacheContext cacheContext = this.contextThreadLocal.get();
        if (cacheContext == null) {
            cacheContext = new CacheContext();
            this.contextThreadLocal.set(cacheContext);
        }
        return cacheContext;
    }

}
