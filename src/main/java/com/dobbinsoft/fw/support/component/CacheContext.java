package com.dobbinsoft.fw.support.component;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CacheContext {

    private List<SuperContext> list = new ArrayList<>();

    /**
     * 从SQL中返回的 返回值
     */
    private Object dbReturn;

    public void append(SuperContext o) {
        list.add(o);
    }

    @Data
    public static abstract class SuperContext {

        /**
         * 被增强的Mapper方法
         */
        private Method method;

        /**
         * 方法入参
         */
        private Object[] args;

    }

    @Data
    public static final class HashPut extends SuperContext {

        private String key;

        private String hashKey;

        private String[] fields;

        private String[] excludeFields;

        private String value;

    }

    @Data
    public static final class HashEvict extends SuperContext {

        private String key;

        private String hashKey;

    }

    @Data
    public static final class KeyPut extends SuperContext {

        private String key;

        private String[] fields;

        private String[] excludeFields;

        private String value;

        private Integer expireSec;

    }

    @Data
    public static final class KeyEvict extends SuperContext {

        private String key;

    }

    @Data
    public static final class ZSetPut extends SuperContext {

        private String key;

        private String value;

        private String source;

    }

    @Data
    public static final class ZSetEvict extends SuperContext {

        private String key;

        private String value;

    }

}
