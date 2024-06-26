package com.dobbinsoft.fw.core.enums;

/**
 * ClassName: EmptyEnums
 *
 */
public enum EmptyEnums implements BaseEnums<Integer> {
    ;

    public Integer getCode() {
        return 0;
    }

    public String getMsg() {
        return null;
    }
}
