package com.dobbinsoft.fw.support.component.open.model;

import lombok.Data;

import java.util.List;

/**
 * ClassName: OPClient
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-04-23
 */
@Data
public class OPClient {

    /**
     * 客户编号
     */
    private String code;

    /**
     * 平台私钥
     */
    private String privateKey1;

    /**
     * 平台公钥
     */
    private String publicKey1;

    /**
     * 对接者私钥
     */
    private String publicKey2;

    /**
     * 客户回调地址
     */
    private String notifyUrl;

    /**
     * 客户可以访问的接口列表
     */
    private List<OPClientPermission> permissionList;

}
