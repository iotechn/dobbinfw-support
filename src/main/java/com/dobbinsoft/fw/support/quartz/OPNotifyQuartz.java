package com.dobbinsoft.fw.support.quartz;

import com.dobbinsoft.fw.support.component.open.OpenPlatform;
import com.dobbinsoft.fw.support.component.open.OpenPlatformStorageStrategy;
import com.dobbinsoft.fw.support.component.open.model.OPNotify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;

/**
 * ClassName: OPQuartz
 * Description: 开放平台通知定时任务
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@EnableScheduling
@Slf4j
public class OPNotifyQuartz {

    @Autowired
    private OpenPlatformStorageStrategy openPlatformStorageStrategy;

    @Autowired
    private OpenPlatform openPlatform;

    /**
     * 每秒执行一次的最大努力通知
     */
    @Scheduled(fixedRate = 1)
    public void tryNotify() {
        if (!openPlatformStorageStrategy.customTryNotify()) {
            List<OPNotify> needNotify = openPlatformStorageStrategy.getNeedNotify();
            for (OPNotify notify : needNotify) {
                try {
                    int res = openPlatform.sendNotify(notify);
                    OPNotify opNotify = new OPNotify();
                    opNotify.setNextNotify(new Date(1000L * 60 * (long) Math.pow(2, notify.getTimes())));
                    opNotify.setTimes(notify.getTimes() + 1);
                    opNotify.setStatus(res);
                    opNotify.setId(notify.getId());
                    openPlatformStorageStrategy.updateNotify(opNotify);
                } catch (Exception e) {
                    log.error("[最大努力通知 定时任务] Item异常", e);
                }
            }
        }
    }

}
