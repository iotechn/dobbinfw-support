package com.dobbinsoft.fw.support.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: PermissionPoint
 * Description: 描述一个权限点
 *
 * @author: e-weichaozheng
 * @date: 2021-03-17
 */
@Data
public class PermissionPoint {

    private String id;

    private String label;

    private String api;

    private List<PermissionPoint> children;

}
