package com.dobbinsoft.fw.support.storage;

import lombok.Data;

/**
 * ClassName: StoragePrivateResult
 * Description: 对象存储私有化保存结果
 */
@Data
public class StoragePrivateResult {

    private String key;

    private boolean suc;

    private String url;

}
