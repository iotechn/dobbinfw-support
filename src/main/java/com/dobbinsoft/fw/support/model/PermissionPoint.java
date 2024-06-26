package com.dobbinsoft.fw.support.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PermissionPoint
 * Description: 描述一个权限点
 *
 */
@Data
public class PermissionPoint {

    private String id;

    private String label;

    private String api;

    private List<PermissionPoint> children;

}
