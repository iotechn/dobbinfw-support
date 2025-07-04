package com.dobbinsoft.fw.support.config.db.cache;

import com.baomidou.mybatisplus.annotation.TableId;
import com.dobbinsoft.fw.core.annotation.CacheKeyCondition;
import com.dobbinsoft.fw.support.component.CacheComponent;
import com.dobbinsoft.fw.support.domain.SuperDO;
import com.dobbinsoft.fw.support.utils.*;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MybatisPlusRedisCache implements Cache {

    private volatile CacheComponent cacheComponent;

    private final Class<?> domainClass;

    private final Field domainId;

    private static final String MYBATIS_CACHE = "MYBATIS_CACHE:";
    private static final String MYBATIS_OBJ_CACHE = "MYBATIS_OBJ_CACHE:";

    private final String id;

    public MybatisPlusRedisCache(String id) {
        try {
            Class<?> clazz = Class.forName(id);
            ParameterizedType genericInterface = (ParameterizedType) clazz.getGenericInterfaces()[0];
            domainClass = (Class<?>) genericInterface.getActualTypeArguments()[0];
            Field f = null;
            Field[] declaredFields = domainClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                if (declaredField.getAnnotation(TableId.class) != null) {
                    f = declaredField;
                    break;
                }
            }
            domainId = f;
            if (f != null) {
                f.setAccessible(Boolean.TRUE);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.id = id;
    }

    private CacheComponent cache() {
        if (IoC.INSTANCE == null) {
            return null;
        }
        if (cacheComponent == null) {
            synchronized (this) {
                if (cacheComponent == null) {
                    cacheComponent = IoC.INSTANCE.getBean(CacheComponent.class);
                }
            }
        }
        return cacheComponent;
    }



    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {
        if (value == null) {
            return;
        }
        CacheComponent cache = cache();
        if (cache == null) {
            return;
        }
        List<?> list = (List<?>) value;
        List<String> conditionIfObj = getConditionIfObj(key);
        if (conditionIfObj != null) {
            // 使用Obj Condition 方式
            if (CollectionUtils.isNotEmpty(list)) {
                Map<String, String> collect = list.stream().collect(Collectors.toMap(k -> {
                    if (k instanceof SuperDO) {
                        return ((SuperDO) k).getId().toString();
                    } else {
                        try {
                            Object o = domainId.get(k);
                            return o.toString();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, JacksonUtil::toJSONString));
                cacheComponent.putHashAll(getRedisCacheObjKey(), collect);
            }
        } else {
            String base64 = SerializationUtils.serializeToString(list);
            cache.putHashRaw(getRedisCacheKey(), key.toString(), base64);
        }

    }

    @Override
    public Object getObject(Object key) {
        CacheComponent cache = cache();
        if (cache == null) {
            return null;
        }
        List<String> conditionIfObj = getConditionIfObj(key);
        if (conditionIfObj != null) {
            // 使用Hash桶
            List<?> hashMultiAsList = cacheComponent.getHashMultiAsList(getRedisCacheObjKey(), conditionIfObj, domainClass);
            if (hashMultiAsList.size() != conditionIfObj.size() || hashMultiAsList.stream().anyMatch(Objects::isNull)) {
                return null;
            }
            return hashMultiAsList;
        } else {
            String base64 = cache.getHashRaw(getRedisCacheKey(), key.toString());
            if (StringUtils.isEmpty(base64)) {
                return null;
            }
            return SerializationUtils.deserializeFromString(base64);
        }
    }

    @Override
    public Object removeObject(Object key) {
        CacheComponent cache = cache();
        if (cache == null) {
            return null;
        }
        cache.delHashKey(getRedisCacheKey(), key.toString());
        return null;
    }

    @Override
    public void clear() {
        CacheComponent cache = cache();
        if (cache == null) {
            return;
        }
        cache.del(getRedisCacheKey());
        cache.del(getRedisCacheObjKey());
    }

    @Override
    public int getSize() {
        CacheComponent cache = cache();
        if (cache == null) {
            return 0;
        }
        return cache.getHashSize(getRedisCacheKey()).intValue() + cache.getHashSize(getRedisCacheObjKey()).intValue();
    }

    public List<String> getConditionIfObj(Object key) {
        if (key instanceof CacheKey) {
            try {
                Field[] declaredFields = key.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    if ("updateList".equals(declaredField.getName())) {
                        declaredField.setAccessible(true);
                        Object o = declaredField.get(key);
                        if (o instanceof List<?> list) {
                            Object first = list.getFirst();
                            if (first instanceof String firstStr) {
                                if (firstStr.endsWith(".selectByIds")
                                  || firstStr.endsWith(".selectById")) {
                                    List<String> objects = new ArrayList<>();
                                    for (int i = 4; i < list.size() - 1; i++) {
                                        // 按ID查询时，不允许重复
                                        String objKey = list.get(i).toString();
                                        if (!objects.contains(objKey)) {
                                            objects.add(objKey);
                                        }
                                    }
                                    return objects;
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException ignored) {

            }
        }
        return null;
    }

    private String getRedisCacheKey() {
        return MYBATIS_CACHE + id + getConditionKey();
    }

    private String getRedisCacheObjKey() {
        return MYBATIS_OBJ_CACHE + id + getConditionKey();
    }

    private String getConditionKey() {
        CacheKeyCondition condition = domainClass.getAnnotation(CacheKeyCondition.class);
        if (condition == null) {
            return StringUtils.EMPTY;
        }
        if (IoC.INSTANCE == null) {
            return StringUtils.EMPTY;
        }
        Class<? extends CacheKeyConditionProvider> clazz = condition.conditionProvider();
        CacheKeyConditionProvider provider = IoC.INSTANCE.getBean(clazz);
        String key = provider.provideKey();
        if (key == null) {
            throw new RuntimeException("@CacheKeyCondition注解的Domain，比如提供ConditionKey");
        }
        return ":" + key;
    }


}
