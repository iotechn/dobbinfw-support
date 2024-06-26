package com.dobbinsoft.fw.support.utils.excel;

import lombok.Data;

import java.util.List;

@Data
public class ExcelData<T> {
    private String fileName;
    private List<T> data;
}