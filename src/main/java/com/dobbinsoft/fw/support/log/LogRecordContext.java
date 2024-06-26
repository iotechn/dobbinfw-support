package com.dobbinsoft.fw.support.log;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务日志记录中间对象
 * 注意名称不能与请入入参的名称相同，也不能等于 "return"
 * 注意不能跨线程使用！
 */
public class LogRecordContext {

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object object) {
        Map<String, Object> map = context.get();
        map.put(key, object);
    }

    public static Map<String, Object> get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

}
