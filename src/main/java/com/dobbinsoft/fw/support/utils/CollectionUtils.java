package com.dobbinsoft.fw.support.utils;

import org.springframework.lang.Nullable;

import java.util.*;

public class CollectionUtils extends org.springframework.util.CollectionUtils {

    public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return !CollectionUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !CollectionUtils.isEmpty(collection);
    }

    public static boolean isEqualCollection(Collection<?> a, Collection<?> b) {
        if (a == null && b == null) {
            // 两个都为空
            return true;
        }
        if (a == null || b == null) {
            // 两个有一个为空
            return false;
        }
        return org.apache.commons.collections4.CollectionUtils.isEqualCollection(a, b);
    }

    public static <O> Collection<O> intersection(Iterable<? extends O> a, Iterable<? extends O> b) {
        return org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }

    public static <O> List<O> intersection(List<? extends O> a, List<? extends O> b) {
        return (List<O>) org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }

    public static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> result = new ArrayList<>();
        if (list == null || size <= 0) {
            return result;
        }

        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }

        return result;
    }


}
