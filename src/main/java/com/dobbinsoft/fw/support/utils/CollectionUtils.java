package com.dobbinsoft.fw.support.utils;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;

public class CollectionUtils extends org.springframework.util.CollectionUtils {

    public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return !CollectionUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    public static boolean isEqualCollection(Collection<?> a, Collection<?> b) {
        return org.apache.commons.collections4.CollectionUtils.isEqualCollection(a, b);
    }


}
