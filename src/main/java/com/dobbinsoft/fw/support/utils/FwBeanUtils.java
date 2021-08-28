package com.dobbinsoft.fw.support.utils;

import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class FwBeanUtils {

    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target);
    }

    public static void copyProperties(Object source, Object target, String ...ignore) {
        BeanUtils.copyProperties(source, target, ignore);
    }

    public static void copyPropertiesFields(Object source, Object target, String ...fields) {
        Object[] objects = Arrays.stream(target.getClass().getFields()).map(item -> item.getName()).filter(item -> {
            boolean exist = false;
            for (String f : fields) {
                if (f.equals(item)) {
                    exist = true;
                }
            }
            return !exist;
        }).collect(Collectors.toList()).toArray();
        BeanUtils.copyProperties(source, target, (String[]) objects);
    }

}
