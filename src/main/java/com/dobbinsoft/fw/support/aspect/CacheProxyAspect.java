package com.dobbinsoft.fw.support.aspect;

import cn.hutool.core.bean.BeanUtil;
import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.component.CacheComponent;
import com.dobbinsoft.fw.support.component.CacheContext;
import com.dobbinsoft.fw.support.utils.SPELUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
public class CacheProxyAspect {

    @Autowired
    private CacheComponent cacheComponent;

    @Pointcut("@annotation(com.dobbinsoft.fw.support.annotation.cache.CacheProxy)")
    public void cachePointCut() {
    }

    /**
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("cachePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 写缓存，必须开启DB事务
        Object result = joinPoint.proceed();
        // 现在事务并没有提交
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 当事务提交后，执行缓存写操作
                CacheContext cacheContext = cacheComponent.getCacheContext();
                // TODO 删除优先级需要高于
//                cacheContext.getList().sort(new Comparator<CacheContext.SuperContext>() {
//                    @Override
//                    public int compare(CacheContext.SuperContext o1, CacheContext.SuperContext o2) {
//
//                        return 0;
//                    }
//                });
                // 根据CacheContext来执行
                for (CacheContext.SuperContext superContext : cacheContext.getList()) {
                    if (superContext instanceof CacheContext.KeyPut) {
                        CacheContext.KeyPut keyPut = (CacheContext.KeyPut) superContext;
                        // parseKey
                        String key = SPELUtils.parseKey(keyPut.getMethod(), keyPut.getArgs(), keyPut.getKey());
                        // parseValue
                        Object value = SPELUtils.parseSpel(keyPut.getMethod(), keyPut.getArgs(), keyPut.getValue(), cacheComponent.getCacheContext().getDbReturn());
                        if (value != null) {
                            Class<?> clazz = value.getClass();
                            if (!Const.IGNORE_PARAM_LIST.contains(clazz)) {
                                Object o = CacheProxyAspect.this.postSaveCacheObj(value, keyPut.getFields(), keyPut.getExcludeFields());
                                cacheComponent.putObj(key, o, keyPut.getExpireSec());
                            } else {
                                cacheComponent.putRaw(key, value.toString(), keyPut.getExpireSec());
                            }
                        }
                    } else if (superContext instanceof CacheContext.HashPut) {
                        CacheContext.HashPut hashPut = (CacheContext.HashPut) superContext;
                        // parseKey
                        String key = SPELUtils.parseKey(hashPut.getMethod(), hashPut.getArgs(), hashPut.getKey());
                        // parseHashKey
                        String hashKey = SPELUtils.parseKey(hashPut.getMethod(), hashPut.getArgs(), hashPut.getHashKey());
                        // parseValue
                        Object value = SPELUtils.parseSpel(hashPut.getMethod(), hashPut.getArgs(), hashPut.getValue(), cacheComponent.getCacheContext().getDbReturn());
                        if (value != null) {
                            Class<?> clazz = value.getClass();
                            if (!Const.IGNORE_PARAM_LIST.contains(clazz)) {
                                Object o = CacheProxyAspect.this.postSaveCacheObj(value, hashPut.getFields(), hashPut.getExcludeFields());
                                cacheComponent.putHashObj(hashKey, key, o);
                            } else {
                                cacheComponent.putHashRaw(hashKey, key, value.toString());
                            }
                        }
                    } else if (superContext instanceof CacheContext.HashEvict) {
                        CacheContext.HashEvict hashEvict = (CacheContext.HashEvict) superContext;
                        // parseKey
                        String key = SPELUtils.parseKey(hashEvict.getMethod(), hashEvict.getArgs(), hashEvict.getKey());
                        // parseHashKey
                        String hashKey = SPELUtils.parseKey(hashEvict.getMethod(), hashEvict.getArgs(), hashEvict.getHashKey());
                        cacheComponent.delHashKey(hashKey, key);
                    } else if (superContext instanceof CacheContext.ZSetPut) {
                        CacheContext.ZSetPut zSetPut = (CacheContext.ZSetPut) superContext;
                        // parseKey
                        String key = SPELUtils.parseKey(zSetPut.getMethod(), zSetPut.getArgs(), zSetPut.getKey());
                        // parseValue
                        String value = SPELUtils.parseKey(zSetPut.getMethod(), zSetPut.getArgs(), zSetPut.getValue());
                        // parseSource
                        String source = SPELUtils.parseKey(zSetPut.getMethod(), zSetPut.getArgs(), zSetPut.getSource());
                        if (source == null) {
                            source = "0";
                        }
                        double sourceValue = Double.parseDouble(source);
                        cacheComponent.putZSet(key, sourceValue, value);
                    } else if (superContext instanceof CacheContext.ZSetEvict) {
                        CacheContext.ZSetEvict zSetEvict = (CacheContext.ZSetEvict) superContext;
                        // parseKey
                        String key = SPELUtils.parseKey(zSetEvict.getMethod(), zSetEvict.getArgs(), zSetEvict.getKey());
                        // parseValue
                        String value = SPELUtils.parseKey(zSetEvict.getMethod(), zSetEvict.getArgs(), zSetEvict.getValue());
                        cacheComponent.removeSetRaw(key, value);
                    }
                }
            }
        });
        return result;
    }

    private Object postSaveCacheObj(Object object, String[] fields, String[] exclude) {
        if (fields.length > 0) {
            Map<String, Object> objectMap = BeanUtil.beanToMap(object);
            Set<String> strings = objectMap.keySet();
            for (String key : strings) {
                boolean exist = false;
                for (String field : fields) {
                    if (key.equals(field)) {
                        exist = true;
                    }
                }
                if (!exist) {
                    objectMap.remove(key);
                }
            }
            return objectMap;
        } else if (exclude.length > 0) {
            Map<String, Object> objectMap = BeanUtil.beanToMap(object);
            for (String field : exclude) {
                objectMap.remove(field);
            }
            return objectMap;
        } else {
            return object;
        }
    }

}
