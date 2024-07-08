package com.dobbinsoft.fw.support.component;

import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tracking Component 目前对PV进行统计，并且
 */
@Component
public class TrackingComponent {

    @Autowired
    private CacheComponent cacheComponent;

    /**
     * 对pv进行计数
     * @param key
     * @param resource
     * @return
     */
    public boolean countPv(String key, String resource) {
        return countPv(key, resource, true);
    }

    /**
     * 对pv进行计数，拆分到日期
     * @param key
     * @param resource
     * @param splitDate
     * @return
     */
    public boolean countPv(String key, String resource, boolean splitDate) {
        if (splitDate) {
            cacheComponent.incrementHashKey(key + ":" + formatLocalDate(LocalDate.now()), resource, 1);
        } else {
            cacheComponent.incrementHashKey(key, resource, 1);
        }
        return true;
    }

    /**
     * 获取PV和
     * @param key
     * @param resource
     * @param startDate
     * @param endDate
     * @return
     */
    public int getSumPv(String key, String resource, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("结束日期必须大于等于开始日期");
        }
        LocalDate currentDate = startDate;
        int count = 0;
        do {
            String hashRaw = cacheComponent.getHashRaw(key + ":" + formatLocalDate(startDate), resource);
            int pv;
            if (StringUtils.isNotEmpty(hashRaw)) {
                pv = Integer.parseInt(hashRaw);
            } else {
                pv = 0;
            }
            count+=pv;
            currentDate = currentDate.plusDays(1);
        } while (currentDate.isBefore(endDate));
        return count;
    }

    /**
     * 获取PV和
     * @param key
     * @param resource
     * @param startDate
     * @return
     */
    public int getSumPv(String key, String resource, LocalDate startDate) {
        return getSumPv(key, resource, startDate, LocalDate.now());
    }

    /**
     * 获取PV和，不按日期拆分的
     * @param key
     * @param resource
     * @return
     */
    public int getSumPv(String key, String resource) {
        String hashRaw = cacheComponent.getHashRaw(key, resource);
        if (StringUtils.isNotEmpty(hashRaw)) {
            return Integer.parseInt(hashRaw);
        } else {
            return 0;
        }
    }

    /**
     * 清除掉从date开始的N天以前的PV
     * @param key
     * @param date
     * @param days
     */
    public void clearPvBefore(String key, LocalDate date, int days) {
        for (int i = 0; i < days; i++) {
            cacheComponent.del(key + ":" + formatLocalDate(date));
            date = date.plusDays(-1);
        }
    }

    /**
     * 每个resource会固定消耗12K内存，建议在 访问量 远大于 资源量的情况下使用。或者资源比较少的情况下使用。
     * UV统计，UV无法自动按照时间拆分，如果需要，请自己在业务系统通过修改resource。
     * @param key
     * @param resource
     * @param accessor
     */
    public void countUv(String key, String resource, String accessor) {
        cacheComponent.addHyperLogLog(key + ":" + resource, accessor);
    }

    /**
     * 获取UV总数
     * @param key
     * @param resource
     * @return
     */
    public long getSumUv(String key, String resource) {
        return cacheComponent.sizeHyperLogLog(key + ":" + resource);
    }



    private String formatLocalDate(LocalDate date) {
        // 创建一个DateTimeFormatter对象，定义要使用的日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 使用DateTimeFormatter对象格式化LocalDate对象
        return date.format(formatter);
    }

}
