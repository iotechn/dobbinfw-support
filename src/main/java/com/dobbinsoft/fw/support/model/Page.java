package com.dobbinsoft.fw.support.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2018-08-15
 * Time: 下午8:12
 */
@Data
@NoArgsConstructor
public class Page<T> implements Serializable, IPage<T> {

    private List<T> items;

    private int pageNo;

    private int pageSize;

    @Getter
    private long count;

    private List<OrderItem> orderItems;

    public Page(List<T> items, int pageNo, int pageSize, long count) {
        this.items = items;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.count = count;
    }

    public long getTotalPageNo() {
        return count / pageSize + (count % pageSize == 0 ? 0 : 1);
    }

    public boolean hasNext() {
        return getPageNo() < getTotalPageNo();
    }

    public boolean hasPrevious() {
        return getPageNo() > 1;
    }

    public String getMsg() {
        return "第" + pageNo + "页,共" + count + "条";
    }
    /** 实现MP的IPage接口，以另一种形式展示 **/

    @Override
    public List<OrderItem> orders() {
        return orderItems;
    }

    @Override
    @JsonIgnore
    public List<T> getRecords() {
        return this.items;
    }

    @Override
    @JsonIgnore
    public IPage<T> setRecords(List<T> records) {
        this.setItems(records);
        return this;
    }

    public long getTotal() {
        return getCount();
    }

    @Override
    public IPage<T> setTotal(long total) {
        this.setCount(total);
        return this;
    }

    @Override
    @JsonIgnore
    public long getSize() {
        return this.getPageSize();
    }

    @Override
    @JsonIgnore
    public IPage<T> setSize(long size) {
        this.setPageSize((int) size);
        return this;
    }

    @Override
    @JsonIgnore
    public long getCurrent() {
        return this.getPageNo();
    }

    @Override
    @JsonIgnore
    public IPage<T> setCurrent(long current) {
        this.setPageNo((int) current);
        return this;
    }

    /**
     * 将分页中的 T 数据类型转换为 R 数据类型
     * @param transMethod
     * @param <R>
     * @return
     */
    public <R> Page<R> trans(Function<T, R> transMethod) {
        this.setItems((List) this.items.stream().map(transMethod).collect(Collectors.toList()));
        return (Page<R>) this;
    }

    /**
     * 正向排序
     * @param columns
     * @return
     */
    public Page<T> ases(String... columns) {
        this.orderItems = OrderItem.ascs(columns);
        return this;
    }

    /**
     * 正向排序
     * @param columns
     * @return
     */
    public Page<T> descs(String... columns) {
        this.orderItems = OrderItem.descs(columns);
        return this;
    }

    public Page<T> sort(boolean isAsc, String...columns) {
        if (isAsc) {
            return ases(columns);
        } else {
            return descs(columns);
        }
    }

    public static <T> Page<T> div(int pageNo, int pageSize, Class<T> clazz) {
        Page<T> page = new Page<T>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }

    // 列表假分页
    public static <T> Page<T> divFake(int pageNo, int pageSize, List<T> list) {
        List<List<T>> partition = CollectionUtils.partition(list, pageSize);
        int index = pageNo - 1;
        List<T> fakeItems;
        if (partition.size() > index) {
            fakeItems = partition.get(index);
        } else {
            fakeItems = Collections.emptyList();
        }
        return new Page<T>(fakeItems, pageNo, pageSize, list.size());
    }

    public <R> Page<R> replace(List<R> items) {
        this.setItems((List) items);
        return (Page<R>) this;
    }

    private static List<?> emptyArray = Collections.emptyList();

    public static Page emptyPage = new Page(emptyArray, 1, 15, 0);

    public static final <T> List<T> emptyPage() {
        return (List<T>) emptyPage;
    }

}
