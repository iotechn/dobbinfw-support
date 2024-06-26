package com.dobbinsoft.fw.support.aspect;

import com.dobbinsoft.fw.support.annotation.AspectCommonCache;
import com.dobbinsoft.fw.support.component.CacheComponent;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description: 基本缓存
 * User: rize
 * Date: 2020/3/28
 * Time: 14:09
 */
@Aspect
@Component
public class RedisCommonCacheAspect {

    @Autowired
    private CacheComponent cacheComponent;

    @Pointcut("@annotation(com.dobbinsoft.fw.support.annotation.AspectCommonCache)")
    public void cachePointCut() {}


    @Around("cachePointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AspectCommonCache annotation = signature.getMethod().getAnnotation(AspectCommonCache.class);
        int[] argIndexes = annotation.argIndex();
        Object[] args = joinPoint.getArgs();
        StringBuilder key = new StringBuilder(annotation.value());
        for (int i = 0; i < argIndexes.length; i++) {
            if (i != 0) {
                key.append(":").append(args[argIndexes[i]]);
            } else {
                key.append(args[argIndexes[i]]);
            }
        }

        // 走缓存
        if (annotation.arrayClass() != Object.class) {
            List<?> objList = cacheComponent.getObjList(key.toString(), annotation.arrayClass());
            if (objList != null) {
                return objList;
            }
        } else {
            Object obj = cacheComponent.getObj(key.toString(), signature.getReturnType());
            if (obj != null) {
                return obj;
            }
        }

        // 走方法
        Object proceed = joinPoint.proceed();
        if (annotation.second() > 0) {
            cacheComponent.putObj(key.toString(), proceed, annotation.second());
        } else {
            cacheComponent.putObj(key.toString(), proceed);
        }
        return proceed;
    }

}
