package com.dobbinsoft.fw.support.rpc;

import java.util.HashMap;
import java.util.Map;

/**
 * Rpc调用上下文, 需要有通用数据在整个调用链中传递时使用
 * 例如：请求的唯一ID，MDC信息/租户隔离信息/当前登录用户等
 *
 * 注意：每次调用完成就会被清空
 */
public class RpcContextHolder {

    private static final ThreadLocal<Map<String, String>> context = new ThreadLocal<>();

    public static void add(String key, String value) {
        Map<String, String> map = context.get();
        if (map == null) {
            map = new HashMap<>();
            context.set(map);
        }
        map.put(key, value);
    }

    public static String get(String key) {
        Map<String, String> map = context.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    public static Map<String, String> getAll() {
        Map<String, String> map = context.get();
        if (map == null) {
            return new HashMap<>();
        }
        return map;
    }

    public static void clear() {
        context.remove();
    }

}
