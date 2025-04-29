package com.dobbinsoft.fw.support.config.db.cache;

import com.dobbinsoft.fw.support.component.CacheComponent;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.IoC;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.cache.Cache;

import java.util.ArrayList;
import java.util.List;

public class MybatisPlusRedisCache implements Cache {

    private volatile CacheComponent cacheComponent;

    private static final String MYBATIS_CACHE = "MYBATIS_CACHE:";

    private final String id;

    public MybatisPlusRedisCache(String id) {
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
        if (CollectionUtils.isEmpty(list)) {
            cache.putHashObj(MYBATIS_CACHE + id, key.toString(), new CacheWrapper(null, 0, ""));
        } else {
            cache.putHashObj(MYBATIS_CACHE + id, key.toString(), new CacheWrapper(list.getFirst().getClass().getCanonicalName(), list.size(), JacksonUtil.toJSONString(value)));
        }
    }

    @Override
    public Object getObject(Object key) {
        CacheComponent cache = cache();
        if (cache == null) {
            return null;
        }
        String json = cache.getHashRaw(MYBATIS_CACHE + id, key.toString());
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        CacheWrapper cacheWrapper = JacksonUtil.parseObject(json, CacheWrapper.class);
        assert cacheWrapper != null;
        if (cacheWrapper.arraySize == 0) {
            return new ArrayList<>();
        }
        try {
            return JacksonUtil.parseArray(cacheWrapper.json, Class.forName(cacheWrapper.getDomainClass()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object removeObject(Object key) {
        CacheComponent cache = cache();
        if (cache == null) {
            return null;
        }
        cache.delHashKey(MYBATIS_CACHE + id, key.toString());
        return null;
    }

    @Override
    public void clear() {
        CacheComponent cache = cache();
        if (cache == null) {
            return;
        }
        cache.del(MYBATIS_CACHE + id);
    }

    @Override
    public int getSize() {
        CacheComponent cache = cache();
        if (cache == null) {
            return 0;
        }
        return cache.getHashSize(MYBATIS_CACHE + id).intValue();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheWrapper {

        private String domainClass;

        private int arraySize;

        private String json;

    }
}
