package com.dobbinsoft.fw.support.component;

import com.dobbinsoft.fw.support.model.Page;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

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

    private static final String NULL_FLAG = "___DOBBIN_OBJECT_IS_NULL___";

    /**
     * 放入不过期不序列化缓存
     *
     * @param key
     * @param value
     */
    public void putRaw(String key, String value) {
        if (value == null) {
            stringRedisTemplate.opsForValue().set(getKey(key), NULL_FLAG);
            return;
        }
        stringRedisTemplate.opsForValue().set(getKey(key), value);
    }

    /**
     * 放入过期不序列化缓存
     *
     * @param key
     * @param value
     * @param expireSec
     */
    //
    public void putRaw(String key, String value, Integer expireSec) {
        stringRedisTemplate.opsForValue().set(getKey(key), value == null ? NULL_FLAG : value, expireSec, TimeUnit.SECONDS);
    }

    /**
     * 直接获取不反序列化缓存
     *
     * @param key
     * @return
     */
    public String getRaw(String key) {
        String raw = stringRedisTemplate.opsForValue().get(getKey(key));
        if (NULL_FLAG.equals(raw)) {
            return null;
        }
        return raw;
    }

    /**
     * 使一个key在N秒后过期
     * @param key
     * @param expireSec
     */
    public void expireKey(String key, Integer expireSec) {
        stringRedisTemplate.expire(key, expireSec, TimeUnit.SECONDS);
    }


    /**
     * 放入对象/集合，进行序列化
     *
     * @param key
     * @param obj
     */
    public void putObj(String key, Object obj) {
        stringRedisTemplate.opsForValue().set(getKey(key), obj == null ? NULL_FLAG : JacksonUtil.toJSONString(obj));
    }

    /**
     * 放入对象/集合，进行序列化，带过期时间
     *
     * @param key
     * @param obj
     * @param expireSec
     */
    public void putObj(String key, Object obj, Integer expireSec) {
        stringRedisTemplate.opsForValue().set(getKey(key), obj == null ? NULL_FLAG : JacksonUtil.toJSONString(obj), expireSec, TimeUnit.SECONDS);
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
        if (StringUtils.isEmpty(json) || NULL_FLAG.equals(json)) {
            return null;
        }
        return JacksonUtil.parseObject(json, clazz);
    }

    public Long incrementKey(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(getKey(key), delta);
    }

    public Long decrementKey(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(getKey(key), -delta);
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
        if (StringUtils.isEmpty(json) || NULL_FLAG.equals(json)) {
            return null;
        }
        return JacksonUtil.parseArray(json, clazz);
    }

    /**
     * 将值放入Hash里
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public void putHashRaw(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(getKey(key), hashKey, value == null ? NULL_FLAG : value);
    }

    /**
     * 设置Hash对象，进行序列化
     *
     * @param key
     * @param hashKey
     * @param obj
     */
    public void putHashObj(String key, String hashKey, Object obj) {
        stringRedisTemplate.opsForHash().put(getKey(key), hashKey, obj == null ? NULL_FLAG : JacksonUtil.toJSONString(obj));
    }

    /**
     * 设置Hash对象，进行序列化
     *
     * @param key
     * @param hashKey
     * @param obj
     * @param expireSec
     */
    public void putHashObj(String key, String hashKey, Object obj, Integer expireSec) {
        String k = getKey(key);
        boolean hasKey = Boolean.TRUE.equals(stringRedisTemplate.hasKey(k));
        stringRedisTemplate.opsForHash().put(k, hashKey, obj == null ? NULL_FLAG : JacksonUtil.toJSONString(obj));
        if (!hasKey) {
            stringRedisTemplate.expire(k, expireSec, TimeUnit.SECONDS);
        }
    }

    /**
     * 增加Hash表中键的字面数值
     *
     * @param key
     * @param hashKey
     * @param delta
     * @return
     */
    public long incrementHashKey(String key, String hashKey, long delta) {
        return stringRedisTemplate.opsForHash().increment(getKey(key), hashKey, delta);
    }

    /**
     * 减少Hash表中字面的数值
     *
     * @param key
     * @param hashKey
     * @param delta
     * @return
     */
    public long decrementHashKey(String key, String hashKey, long delta) {
        return stringRedisTemplate.opsForHash().increment(getKey(key), hashKey, -delta);
    }

    /**
     * 获取Hash值，不进行序列化
     *
     * @param key
     * @param hashKey
     * @return
     */
    public String getHashRaw(String key, String hashKey) {
        String o = (String) stringRedisTemplate.opsForHash().get(getKey(key), hashKey);
        if (StringUtils.isEmpty(o) || NULL_FLAG.equals(o)) {
            return null;
        }
        return o;
    }

    /**
     * 获取Hash值，带反序列化
     *
     * @param key
     * @param hashKey
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getHashObj(String key, String hashKey, Class<T> clazz) {
        String o = (String) stringRedisTemplate.opsForHash().get(getKey(key), hashKey);
        if (StringUtils.isEmpty(o) || NULL_FLAG.equals(o)) {
            return null;
        }
        return JacksonUtil.parseObject(o, clazz);
    }

    /**
     * 获取Hash值，以数组的形式反序列化
     *
     * @param key
     * @param hashKey
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getHashList(String key, String hashKey, Class<T> clazz) {
        String o = (String) stringRedisTemplate.opsForHash().get(getKey(key), hashKey);
        if (StringUtils.isEmpty(o) || NULL_FLAG.equals(o)) {
            return null;
        }
        return JacksonUtil.parseArray(o, clazz);
    }

    /**
     * 批量获取Hash表里面的值
     *
     * @param key 桶的名字
     * @param hashKeys String类型键集合 Collection《String》
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> getHashMultiAsList(String key, Collection hashKeys, Class<T> clazz) {
        List<String> list = stringRedisTemplate.opsForHash().multiGet(getKey(key), hashKeys);
        return list.stream().filter(item -> StringUtils.isNotEmpty(item) && !NULL_FLAG.equals(item)).map(item -> JacksonUtil.parseObject(item, clazz)).collect(Collectors.toList());
    }

    /**
     * 批量获取Hash值 列表
     *
     * @param key
     * @param hashKeys
     * @return
     */
    public List<String> getHashMultiAsRawList(String key, Collection hashKeys) {
        List<String> list = stringRedisTemplate.opsForHash().multiGet(getKey(key), hashKeys);
        return list.stream().map(item -> NULL_FLAG.equals(item) ? null : item).toList();
    }

    /**
     * 删除Hash值
     *
     * @param key
     * @param hashKey
     */
    public void delHashKey(String key, String hashKey) {
        stringRedisTemplate.opsForHash().delete(getKey(key), hashKey);
    }

    public void putHashAll(String key, Map<String, String> map, Integer expireSec) {
        String k = getKey(key);
        for (String s : map.keySet()) {
            map.putIfAbsent(s, NULL_FLAG);
        }
        stringRedisTemplate.opsForHash().putAll(k, map);
        stringRedisTemplate.expire(k, expireSec, TimeUnit.SECONDS);
    }

    public Map<String,String> getHashAll(String key) {
        String k = getKey(key);
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(k))) {
            return null;
        }
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(k);
        Set<Object> keys = entries.keySet();
        for (Object o : keys) {
            Object tmp = entries.get(o);
            if (NULL_FLAG.equals(tmp)) {
                entries.put(o, null);
            }
        }
        return (Map)entries;
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

    public boolean addPoint(String key, Point point, String member) {
        return stringRedisTemplate.opsForGeo().add(key, point, member) > 0;
    }

    public boolean delPoint(String key, String... member) {
        return stringRedisTemplate.opsForGeo().remove(key, member) > 0;
    }

    public List<String> searchNearby(String key, Point point, Integer distanceMeters) {
        GeoOperations<String, String> geoOperations = stringRedisTemplate.opsForGeo();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOperations.radius(key, new Circle(point, distanceMeters),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates().sortAscending());
        List<String> arrayList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            RedisGeoCommands.GeoLocation<String> content = result.getContent();
            arrayList.add(content.getName());
        }
        return arrayList;
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
     * 基数统计 计数
     * @param key
     * @param values
     * @return
     */
    public boolean addHyperLogLog(String key, String ...values) {
        return stringRedisTemplate.opsForHyperLogLog().add(key, values) != 0;
    }

    /**
     * 基数统计 读取
     * @param key
     * @return
     */
    public long sizeHyperLogLog(String key) {
        return stringRedisTemplate.opsForHyperLogLog().size(key);
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

    public Collection<String> getKeys(Collection<String> keys) {
        if (beforeGetCacheKey != null) {
            return keys.stream().map(item -> {
                return beforeGetCacheKey.getKey(item);
            }).collect(Collectors.toList());
        }
        return keys;
    }

}
