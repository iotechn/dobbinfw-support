package com.dobbinsoft.fw.support.utils.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExcelColumDecorator {

    /**
     * 第N列 从0开始计数
     */
    private Integer index;

    /**
     * 装饰单选枚举下拉框
     */
    private List<String> enums;

}
