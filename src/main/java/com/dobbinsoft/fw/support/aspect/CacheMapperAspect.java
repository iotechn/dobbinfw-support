package com.dobbinsoft.fw.support.aspect;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.support.annotation.cache.*;
import com.dobbinsoft.fw.support.component.CacheComponent;
import com.dobbinsoft.fw.support.component.CacheContext;
import com.dobbinsoft.fw.support.model.Page;
import com.dobbinsoft.fw.support.utils.FwBeanUtils;
import com.dobbinsoft.fw.support.utils.SPELUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//@Aspect
//@Component
public class CacheMapperAspect {

    @Autowired
    private CacheComponent cacheComponent;

    @Pointcut("execution(public * org.mybatis.spring.mapper.MapperFactoryBean.getObject())")
    public void cachePointCut() {}

    @Around("cachePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object mapper = joinPoint.proceed();
        // 为Mapper创造代理对象
        Class<?>[] interfaces = mapper.getClass().getInterfaces();
        Object o = Proxy.newProxyInstance(mapper.getClass().getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    Annotation[] annotations = method.getAnnotations();
                    int readType = 0;
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof CacheAssemble || annotation instanceof CacheAssembles) {
                            readType = 1;
                            break;
                        } else if (annotation instanceof CacheAssembleArray) {
                            readType = 2;
                            break;
                        }
                    }
                    // 区分是读操作 | 写操作
                    if (readType > 0) {
                        // 尝试从缓存读取
                        Class<?> returnType = method.getReturnType();
                        if (readType == 1) {
                            // 读对象
                            CacheAssembles cacheAssembles = method.getAnnotation(CacheAssembles.class);
                            if (cacheAssembles != null) {
                                Object newInstance = returnType.newInstance();
                                CacheAssemble[] values = cacheAssembles.value();
                                Object object = null;
                                for (CacheAssemble cacheAssemble : values) {
                                    object = readCacheObj(cacheAssemble, method, args, returnType, newInstance);
                                }
                                if (object != null) return object;
                            } else {
                                CacheAssemble cacheAssemble = method.getAnnotation(CacheAssemble.class);
                                Object object = readCacheObj(cacheAssemble, method, args, returnType, null);
                                if (object != null) return object;
                            }
                        } else {
                            // 读数组 是一定命中的
                            return readCacheArray(method, args);
                        }
                        Object invoke = method.invoke(mapper, args);
                        cacheComponent.getCacheContext().setDbReturn(invoke);
                        return invoke;
                    } else {
                        // 描述如何写缓存
                        CacheMapperAspect.this.saveCacheContext(method, args);
                        return method.invoke(mapper, args);
                    }
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                } catch (Exception e) {
                    throw e;
                }
            }
        });
        return o;
    }

    /**
     * 从ZSet中读取数据，并映射成页码、列表
     * @param method
     * @param args
     * @return
     */
    private Object readCacheArray(Method method, Object[] args) {
        CacheAssembleArray cacheAssembleArray = method.getAnnotation(CacheAssembleArray.class);
        String[] mappingStrings = cacheAssembleArray.sortMapping();
        String sort = SPELUtils.parseKey(method, args, cacheAssembleArray.sort());
        String key = null;
        if (mappingStrings.length > 0 && !ObjectUtils.isEmpty(sort)) {
            for (String mappingStr : mappingStrings) {
                String[] split = mappingStr.split("\\|");
                if (split[1].equals(sort)) {
                    key = SPELUtils.parseKey(method, args, split[0]);
                    break;
                }
            }
        }
        // Key 设置需要确定Mapping中是否命中
        if (ObjectUtils.isEmpty(key)) {
            key = SPELUtils.parseKey(method, args, cacheAssembleArray.key());
        }
        String page = SPELUtils.parseKey(method, args, cacheAssembleArray.page());
        String limit = SPELUtils.parseKey(method, args, cacheAssembleArray.limit());
        boolean isAsc = true;
        if (!ObjectUtils.isEmpty(cacheAssembleArray.isAsc())) {
            isAsc = Boolean.parseBoolean(SPELUtils.parseKey(method, args, cacheAssembleArray.isAsc()));
        }
        if (!ObjectUtils.isEmpty(page) && !ObjectUtils.isEmpty(limit)) {
            // 读页码
            Page<String> zSetPage = cacheComponent.getZSetPage(key, Integer.parseInt(page), Integer.parseInt(limit), isAsc);
            return zSetPage.trans(item -> {
                Object hashObj = cacheComponent.getHashObj(cacheAssembleArray.jump(), item, cacheAssembleArray.arrayClass());
                return hashObj;
            });
        } else {
            Set<String> zSetList = cacheComponent.getZSetList(key, isAsc);
            return zSetList.stream().map(item -> {
                Object hashObj = cacheComponent.getHashObj(cacheAssembleArray.jump(), item, cacheAssembleArray.arrayClass());
                return hashObj;
            }).collect(Collectors.toList());
        }
    }

    /**
     * 从缓存中读单个对象
     * @param cacheAssemble
     * @param method
     * @param args
     * @param returnType
     * @return
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object readCacheObj(CacheAssemble cacheAssemble, Method method, Object[] args, Class<?> returnType, Object baseObj) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        // 读对象
        String key = SPELUtils.parseKey(method, args, cacheAssemble.key());
        String hashKey = cacheAssemble.hashKey();
        if (!ObjectUtils.isEmpty(hashKey)) {
            hashKey = SPELUtils.parseKey(method, args, hashKey);
            // 读Hash
            if (Const.IGNORE_PARAM_LIST.contains(returnType)) {
                String hashRaw = cacheComponent.getHashRaw(hashKey, key);
                if (hashRaw != null) {
                    // 命中缓存
                    Constructor<?> constructor = returnType.getConstructor(String.class);
                    return constructor.newInstance(hashRaw);
                } else {
                    this.appendHashPutOnRead(cacheAssemble, method, args, cacheAssemble.key(), cacheAssemble.hashKey());
                    return null;
                }
            } else {
                if (cacheAssemble.arrayClass() != Object.class) {
                    List hashList = cacheComponent.getHashList(hashKey, key, cacheAssemble.arrayClass());
                    if (hashList != null) {
                        return hashList;
                    } else {
                        this.appendHashPutOnRead(cacheAssemble, method, args, cacheAssemble.key(), cacheAssemble.hashKey());
                        return null;
                    }
                } else {
                    Object hashObj = cacheComponent.getHashObj(hashKey, key, returnType);
                    if (hashObj != null) {
                        return postCacheHit(cacheAssemble, baseObj, hashKey, hashObj);
                    } else {
                        this.appendHashPutOnRead(cacheAssemble, method, args, cacheAssemble.key(), cacheAssemble.hashKey());
                        return null;
                    }
                }
            }
        } else {
            // 读KV
            if (Const.IGNORE_PARAM_LIST.contains(returnType)) {
                String raw = cacheComponent.getRaw(key);
                if (raw != null) {
                    // 命中缓存
                    Constructor<?> constructor = returnType.getConstructor(String.class);
                    return constructor.newInstance(raw);
                } else {
                    this.appendKeyOnRead(cacheAssemble, method, args);
                    return null;
                }
            } else {
                if (cacheAssemble.arrayClass() != Object.class) {
                    List objList = cacheComponent.getObjList(key, cacheAssemble.arrayClass());
                    if (objList != null) {
                        return objList;
                    } else {
                        this.appendKeyOnRead(cacheAssemble, method, args);
                        return null;
                    }
                } else {
                    Object obj = cacheComponent.getObj(key, returnType);
                    if (obj != null) {
                        // 命中缓存
                        return postCacheHit(cacheAssemble, baseObj, hashKey, obj);
                    } else {
                        this.appendKeyOnRead(cacheAssemble, method, args);
                        return null;
                    }
                }
            }
        }
    }

    /**
     * 在读取时 若缓存未命中 写缓存
     * @param cacheAssemble
     * @param method
     * @param args
     */
    private void appendKeyOnRead(CacheAssemble cacheAssemble, Method method, Object[] args) {
        CacheContext.KeyPut keyPut = new CacheContext.KeyPut();
        keyPut.setMethod(method);
        keyPut.setArgs(args);
        keyPut.setExpireSec(cacheAssemble.expireSec());
        keyPut.setKey(cacheAssemble.key());
        keyPut.setFields(cacheAssemble.fields());
        keyPut.setExcludeFields(cacheAssemble.excludeFields());
        keyPut.setValue("#return");
        cacheComponent.getCacheContext().append(keyPut);
    }

    /**
     * 在读取时 若缓存未命中 写缓存
     * @param cacheAssemble
     * @param method
     * @param args
     * @param key
     * @param hashKey
     */
    private void appendHashPutOnRead(CacheAssemble cacheAssemble, Method method, Object[] args, String key, String hashKey) {
        // 未命中缓存，根据读取方式去设置
        CacheContext.HashPut hashPut = new CacheContext.HashPut();
        hashPut.setMethod(method);
        hashPut.setArgs(args);
        hashPut.setFields(cacheAssemble.fields());
        hashPut.setExcludeFields(cacheAssemble.excludeFields());
        hashPut.setKey(key);
        hashPut.setHashKey(hashKey);
        hashPut.setValue("#return");
        cacheComponent.getCacheContext().append(hashPut);
    }

    /**
     * 缓存命中后置处理
     * @param cacheAssemble
     * @param baseObj
     * @param hashKey
     * @param hashObj
     * @return
     */
    private Object postCacheHit(CacheAssemble cacheAssemble, Object baseObj, String hashKey, Object hashObj) {
        // 命中缓存
        if (baseObj != null) {
            if (cacheAssemble.fields().length > 0) {
                FwBeanUtils.copyPropertiesFields(hashObj, baseObj, cacheAssemble.fields());
            } else if (cacheAssemble.excludeFields().length > 0) {
                FwBeanUtils.copyProperties(hashKey, baseObj, cacheAssemble.excludeFields());
            }
            return baseObj;
        }
        return hashObj;
    }

    /**
     * 保存缓存上下文
     * @param method
     * @param args
     */
    private void saveCacheContext(Method method, Object[] args) {
        // 保存缓存上下文
        Annotation[] annotations = method.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof CacheHashPut) {
                appendHashPut(method, args, (CacheHashPut) annotation);
            } else if (annotation instanceof CacheKeyPut) {
                appendKeyPut(method, args, (CacheKeyPut) annotation);
            } else if (annotation instanceof CacheZSetPut) {
                appendZSetPut(method, args, (CacheZSetPut) annotation);
            } else if (annotation instanceof CacheKeyEvict) {
                appendKeyEvict(method, args, (CacheKeyEvict) annotation);
            } else if (annotation instanceof CacheHashEvict) {
                appendHashEvict(method, args, (CacheHashEvict) annotation);
            } else if (annotation instanceof CacheZSetEvict) {
                appendZSetEvict(method, args, (CacheZSetEvict) annotation);
            } else if (annotation instanceof CacheHashPuts) {
                for (CacheHashPut cacheHashPut : ((CacheHashPuts)annotation).value()) {
                    appendHashPut(method, args, cacheHashPut);
                }
            } else if (annotation instanceof CacheKeyPuts) {
                for (CacheKeyPut cacheKeyPut : ((CacheKeyPuts) annotation).value()) {
                    appendKeyPut(method, args, cacheKeyPut);
                }
            } else if (annotation instanceof CacheZSetPuts) {
                for (CacheZSetPut cacheZSetPut : ((CacheZSetPuts) annotation).value()) {
                    appendZSetPut(method, args, cacheZSetPut);
                }
            } else if (annotation instanceof CacheKeyEvicts) {
                for (CacheKeyEvict cacheKeyEvict : ((CacheKeyEvicts) annotation).value()) {
                    appendKeyEvict(method, args, cacheKeyEvict);
                }
            } else if (annotation instanceof CacheHashEvicts) {
                for (CacheHashEvict cacheHashEvict : ((CacheHashEvicts) annotation).value()) {
                    appendHashEvict(method, args, cacheHashEvict);
                }
            } else if (annotation instanceof CacheZSetEvicts) {
                for (CacheZSetEvict cacheZSetEvict : ((CacheZSetEvicts) annotation).value()) {
                    appendZSetEvict(method, args, cacheZSetEvict);
                }
            }
        }
    }

    /**
     * 追加ZSet无效
     * @param method
     * @param args
     * @param annotation
     */
    private void appendZSetEvict(Method method, Object[] args, CacheZSetEvict annotation) {
        CacheContext.ZSetEvict zSetEvict = new CacheContext.ZSetEvict();
        zSetEvict.setMethod(method);
        zSetEvict.setArgs(args);
        zSetEvict.setKey(annotation.key());
        zSetEvict.setValue(annotation.value());
        cacheComponent.getCacheContext().append(zSetEvict);
    }

    /**
     * 描述如何写缓存 之 使 Hash 无效
     * @param method
     * @param args
     * @param annotation
     */
    private void appendHashEvict(Method method, Object[] args, CacheHashEvict annotation) {
        CacheContext.HashEvict hashEvict = new CacheContext.HashEvict();
        hashEvict.setMethod(method);
        hashEvict.setArgs(args);
        hashEvict.setKey(annotation.key());
        hashEvict.setHashKey(annotation.hashKey());
        cacheComponent.getCacheContext().append(hashEvict);
    }

    /**
     * 描述如何写缓存 之 使 Key 无效
     * @param method
     * @param args
     * @param annotation
     */
    private void appendKeyEvict(Method method, Object[] args, CacheKeyEvict annotation) {
        CacheContext.KeyEvict keyEvict = new CacheContext.KeyEvict();
        keyEvict.setMethod(method);
        keyEvict.setArgs(args);
        keyEvict.setKey(annotation.key());
        cacheComponent.getCacheContext().append(keyEvict);
    }

    /**
     * 描述如何写缓存 之 追加 ZSet 写缓存
     * @param method
     * @param args
     * @param annotation
     */
    private void appendZSetPut(Method method, Object[] args, CacheZSetPut annotation) {
        CacheZSetPut cacheZSetPut = annotation;
        CacheContext.ZSetPut zSetPut = new CacheContext.ZSetPut();
        zSetPut.setKey(cacheZSetPut.key());
        zSetPut.setValue(cacheZSetPut.value());
        zSetPut.setSource(cacheZSetPut.source());
        zSetPut.setMethod(method);
        zSetPut.setArgs(args);
        cacheComponent.getCacheContext().append(zSetPut);
    }

    /**
     * 描述如何写缓存 之 追加 Key-Value写缓存
     * @param method
     * @param args
     * @param annotation
     */
    private void appendKeyPut(Method method, Object[] args, CacheKeyPut annotation) {
        CacheKeyPut cacheKeyPut = annotation;
        CacheContext.KeyPut keyPut = new CacheContext.KeyPut();
        keyPut.setKey(cacheKeyPut.key());
        keyPut.setValue(cacheKeyPut.value());
        keyPut.setFields(cacheKeyPut.fields());
        keyPut.setExcludeFields(cacheKeyPut.excludeFields());
        keyPut.setMethod(method);
        keyPut.setArgs(args);
        keyPut.setExpireSec(cacheKeyPut.expireSec());
        cacheComponent.getCacheContext().append(keyPut);
    }

    /**
     * 描述如何写缓存 之 追加 Hash写缓存
     * @param method
     * @param args
     * @param annotation
     */
    private void appendHashPut(Method method, Object[] args, CacheHashPut annotation) {
        CacheHashPut cacheHashPut = annotation;
        CacheContext.HashPut hashPut = new CacheContext.HashPut();
        hashPut.setKey(cacheHashPut.key());
        hashPut.setHashKey(cacheHashPut.hashKey());
        hashPut.setValue(cacheHashPut.value());
        hashPut.setFields(cacheHashPut.fields());
        hashPut.setExcludeFields(cacheHashPut.excludeFields());
        hashPut.setMethod(method);
        hashPut.setArgs(args);
        cacheComponent.getCacheContext().append(hashPut);
    }

}
