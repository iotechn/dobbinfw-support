package com.dobbinsoft.fw.support.component;

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
     * pv
     * @param key
     * @param resource
     * @return
     */
    public boolean countPv(String key, String resource) {
        cacheComponent.incrementHashKey(key + ":" + formatLocalDate(LocalDate.now()), resource, 1);
        return true;
    }

    public int sumPv(String key, String resource, LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("结束日期必须大于等于开始日期");
        }
        LocalDate currentDate = startDate;
        int count = 0;
        do {
            String hashRaw = cacheComponent.getHashRaw(key + ":" + formatLocalDate(startDate), resource);
            int pv;
            if (hashRaw != null && !"".equals(hashRaw)) {
                pv = Integer.parseInt(hashRaw);
            } else {
                pv = 0;
            }
            count+=pv;
            currentDate = currentDate.plusDays(1);
        } while (currentDate.isBefore(endDate));
        return count;

    }

    public int sumPv(String key, String resource, LocalDate startDate) {
        return sumPv(key, resource, startDate, LocalDate.now());
    }

    public void clearBefore(String key, LocalDate date, int days) {
        for (int i = 0; i < days; i++) {
            cacheComponent.del(key + ":" + formatLocalDate(date));
            date = date.plusDays(-1);
        }
    }

    private String formatLocalDate(LocalDate date) {
        // 创建一个DateTimeFormatter对象，定义要使用的日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        // 使用DateTimeFormatter对象格式化LocalDate对象
        return date.format(formatter);
    }

}
