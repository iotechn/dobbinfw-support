package com.dobbinsoft.fw.support.utils.excel;

import com.dobbinsoft.fw.support.model.Page;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ExcelBigExportAdapter<T> {

    protected AtomicInteger pageNoAtomic = new AtomicInteger(1);

    public abstract Class<T> clazz();

    /**
     *
     * @return 允许返回空页，不允许返回null
     */
    public abstract Page<T> getData(int pageNo);

    /**
     * @return 保留N行Row在内存中。也就是滑动窗口大小，越大导出越快，占用内存越多。
     */
    public int windowSize() {
        return 200;
    }

    /**
     *
     * @return 页面自增器，每次getData后，会自动+1，初始值为1
     */
    public AtomicInteger getPageNo() {
        return pageNoAtomic;
    }

}
