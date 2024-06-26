package com.dobbinsoft.fw.support.utils.excel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelImage {

    private byte[] bytes;

    // 会从中获取扩展名，如果为空，则默认为png
    private String name;

}
