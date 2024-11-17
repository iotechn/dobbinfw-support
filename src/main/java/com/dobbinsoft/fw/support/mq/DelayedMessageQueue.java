package com.dobbinsoft.fw.support.mq;

import java.time.LocalDateTime;

public interface DelayedMessageQueue {


    /**
     * RedisNotifyDelayedMessageQueueImpl专用，集群实现
     * 这两个都会被拼接为 TASK:(随机码):CODE:VALUE 当成key存入redis中，因为回调时只会返回key，而不会返回key对应的值
     * @param code 回调时用来选择的Handler的CODE
     * @param value 回调时使用的值
     * @param delay 多少秒后调用
     * @return
     */
    public Boolean publishTask(Integer code, String value, Integer delay);

    /**
     * RedisNotifyDelayedMessageQueueImpl专用，集群实现
     * 这两个都会被拼接为 TASK:(随机码):CODE:VALUE 当成key存入redis中，因为回调时只会返回key，而不会返回key对应的值
     * @param code 回调时用来选择的Handler的CODE
     * @param value 回调时使用的值
     * @param executeTime 执行的绝对时间
     * @return
     */
    public Boolean publishTaskAt(Integer code, String value, LocalDateTime executeTime);

    /**
     * 删除已有任务
     * @param code
     * @param value
     * @return
     */
    public Boolean deleteTask(Integer code, String value);

    /**
     * 获取指定任务还有多少时间执行，如果不存在，返回-2
     * @param code
     * @param value
     * @return
     */
    public Long getTaskTime(Integer code, String value);
}
