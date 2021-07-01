package com.dobbinsoft.fw.support.component.open.model;

import com.dobbinsoft.fw.core.enums.BaseEnums;

public enum OPNotifyStatusType implements BaseEnums<Integer> {
    OK(1, "推送完成"),
    FAIL(0, "推送失败")
    ;

    private Integer code;

    private String msg;

    OPNotifyStatusType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
