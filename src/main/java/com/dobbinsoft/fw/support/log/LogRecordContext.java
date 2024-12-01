package com.dobbinsoft.fw.support.log;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 业务日志记录中间对象
 * 注意名称不能与请入入参的名称相同，也不能等于 "return"
 * 注意不能跨线程使用！
 */
public class LogRecordContext {

    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<LogRefer> logReferContext = ThreadLocal.withInitial(LogRefer::new);


    public static void put(String key, Object object) {
        Map<String, Object> map = context.get();
        map.put(key, object);
    }

    public static void putRefer(Integer referType, Long referId) {
        logReferContext.get().setReferType(referType);
        logReferContext.get().setReferId(referId);
    }

    public static LogRefer getRefer() {
        return logReferContext.get();
    }

    public static Map<String, Object> get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
        logReferContext.remove();
    }

    @Getter
    @Setter
    public static class LogRefer {

        private Integer referType;

        private Long referId;

    }

}
