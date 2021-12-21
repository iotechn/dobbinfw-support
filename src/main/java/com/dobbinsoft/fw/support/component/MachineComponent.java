package com.dobbinsoft.fw.support.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MachineComponent {

    @Autowired
    private StringRedisTemplate lockRedisTemplate;

    private Integer machineNo;

    private static final String MACHINE_PREFIX = "MACHINE_PREFIX_";

    public boolean isInit() {
        return this.machineNo != null;
    }

    /**
     * 从集群中获取标号，优先使用空缺编号，若不存在，则向后延申，从1开始编号
     *
     * @return
     */
    public Integer getMachineNo() {
        if (this.machineNo == null) {
            synchronized (MachineComponent.class) {
                if (this.machineNo == null) {
                    for (int i = 1; i < Integer.MAX_VALUE; i++) {
                        Boolean suc = lockRedisTemplate.opsForValue().setIfAbsent(MACHINE_PREFIX + i, "" + i);
                        if (suc) {
                            lockRedisTemplate.expire(MACHINE_PREFIX + i, 15, TimeUnit.MINUTES);
                            this.machineNo = i;
                            break;
                        }
                    }
                }
            }
        }
        return this.machineNo;
    }

    /**
     * 续约
     */
    public void renew() {
        if (this.machineNo == null) {
            throw new RuntimeException("续约失败，请先获取机器号");
        }
        lockRedisTemplate.expire(MACHINE_PREFIX + this.machineNo, 15, TimeUnit.MINUTES);
    }

    /**
     * 主动释放机器号
     */
    public void release() {
        lockRedisTemplate.delete(MACHINE_PREFIX + this.machineNo);
    }


}
