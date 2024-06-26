package com.dobbinsoft.fw.support.storage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageListRequest {

    private String prefix;

    private Integer rows;

    /**
     * 后一页的标记
     */
    private String nextMarker;

}
