package com.dobbinsoft.fw.support.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * ClassName: ClassDescription
 * Description: 类文件描述
 */
@Data
public class ClassDescription implements Serializable {

    /**
     * 类型 1: 接口；  2. 类
     */
    private Integer type;

    /**
     * 标记是哪个系统的
     */
    private String app;

    /**
     * 标记是哪个场景
     */
    private String scene;

    private Map<String, byte[]> byteMap;

}
