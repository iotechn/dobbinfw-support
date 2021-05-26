package com.dobbinsoft.fw.support.context;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * ClassName: QueryContext
 * Description: 查询上下文
 *
 * @author: e-weichaozheng
 * @date: 2021-04-21
 */
public class QueryContext {

    public static ThreadLocal<QueryWrapper> tl = new ThreadLocal<>();

    public static void set(QueryWrapper wrapper) {
        tl.set(wrapper);
    }

    public static QueryWrapper get() {
        return tl.get();
    }

    public static void clear() {
        tl.remove();
    }

}
