package com.dobbinsoft.fw.support.model;

import com.dobbinsoft.fw.core.annotation.doc.ApiField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScopePoint {

    @ApiField(description = "权限范围名称")
    private String name;

    @ApiField(description = "数据库字段")
    private String dbField;

    @ApiField(description = "隔离值")
    private Long value;

    private List<ScopePoint> children;

}
