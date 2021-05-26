package com.dobbinsoft.fw.support.component.open.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * ClassName: OPNotify
 * Description: 最大努力通知实例
 *
 * @author: e-weichaozheng
 * @date: 2021-04-25
 */
@Data
public class OPNotify {

    private String id;

    private String clientCode;

    private List<String> params;

    /**
     * 已经推送次数
     */
    private Integer times;

    /**
     * 是否已经通知
     */
    private Integer status;

    /**
     * 下一次通知时间
     */
    private Date nextNotify;

}
