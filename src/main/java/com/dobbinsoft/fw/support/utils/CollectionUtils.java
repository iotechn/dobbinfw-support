package com.dobbinsoft.fw.support.utils;

import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

    public static <O> Collection<O> intersection(Iterable<? extends O> a, Iterable<? extends O> b) {
        return org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }

    public static <O> List<O> intersection(List<? extends O> a, List<? extends O> b) {
        return (List<O>) org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }


}
