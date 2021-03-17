package com.dobbinsoft.fw.support.domain;

import lombok.Data;

/**
 * Description: 为方便使用系统，将部分静态配置动态化，例如微信相关的配置，OSS，短信等配置
 * User: rize
 * Date: 2020/8/5
 * Time: 11:28
 */
@Data
public class DynamicConfigDO extends SuperDO {

    private String key;

    private String value;

}
