package com.dobbinsoft.fw.support.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

@Slf4j
public class BeanUtils extends org.springframework.beans.BeanUtils {

    /**
     * 清理掉对象的空串属性
     * @param object
     */
    public static void clearEmptyCondition(Object object) {
        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String getter = "get" + StringUtils.upperFirst(field.getName());
                Method getterMethod = clazz.getMethod(getter);
                Object res = getterMethod.invoke(object);
                if (res instanceof String) {
                    // 若是返回String
                    if ("".equals(res)) {
                        //设置为空
                        String setter = "set" + StringUtils.upperFirst(field.getName());
                        Method setterMethod = clazz.getMethod(setter, String.class);
                        setterMethod.invoke(object, (Object) null);
                    }
                } else if (res instanceof List) {
                    if (CollectionUtils.isEmpty((Collection<?>) res)) {
                        //设置为空
                        String setter = "set" + StringUtils.upperFirst(field.getName());
                        Method setterMethod = clazz.getMethod(setter, List.class);
                        setterMethod.invoke(object, (Object) null);
                    }
                }
            }
        } catch (Exception e) {
            log.info("[清理空字符串] 异常", e);
        }
    }

}
