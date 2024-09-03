package com.dobbinsoft.fw.core;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

public class Const {

    public static final Set<Class> IGNORE_PARAM_LIST = new HashSet<Class>();
    public static final Set<Class> IGNORE_DEEP_PARAM_LIST = new HashSet<Class>();

    static {
        IGNORE_PARAM_LIST.add(boolean.class);
        IGNORE_PARAM_LIST.add(byte.class);
        IGNORE_PARAM_LIST.add(char.class);
        IGNORE_PARAM_LIST.add(short.class);
        IGNORE_PARAM_LIST.add(int.class);
        IGNORE_PARAM_LIST.add(long.class);
        IGNORE_PARAM_LIST.add(float.class);
        IGNORE_PARAM_LIST.add(double.class);
        IGNORE_PARAM_LIST.add(Byte.class);
        IGNORE_PARAM_LIST.add(Character.class);
        IGNORE_PARAM_LIST.add(Short.class);
        IGNORE_PARAM_LIST.add(Integer.class);
        IGNORE_PARAM_LIST.add(Long.class);
        IGNORE_PARAM_LIST.add(String.class);
        IGNORE_PARAM_LIST.add(Float.class);
        IGNORE_PARAM_LIST.add(Double.class);
        IGNORE_PARAM_LIST.add(Boolean.class);
    }

    static {
        IGNORE_DEEP_PARAM_LIST.add(LocalDate.class);
        IGNORE_DEEP_PARAM_LIST.add(LocalTime.class);
        IGNORE_DEEP_PARAM_LIST.add(LocalDateTime.class);
        IGNORE_DEEP_PARAM_LIST.add(BigDecimal.class);
    }

    public static final int CACHE_ONE_DAY = 60 * 60 * 24;

    public static final int CACHE_ONE_YEAR = 60 * 60 * 24 * 365;

    public static final String RPC_HEADER = "RPCHEADER";

    public static final String RPC_CONTEXT_JSON = "RPCCONTEXT";

    public static final String RPC_SYSTEM_ID = "RPCSYSTEMID";

    public static final String HTTP_TRACE_HEADER = "TRACE";


    public static final String USER_ACCESS_TOKEN = "ACCESSTOKEN";

    public static final String USER_REDIS_PREFIX = "USER_SESSION_";

    public static final String ADMIN_ACCESS_TOKEN = "ADMINTOKEN";

    public static final String ADMIN_REDIS_PREFIX = "ADMIN_SESSION_";

    public static final String CUSTOM_REDIS_PREFIX = "CUSTOM_SESSION_";



}
