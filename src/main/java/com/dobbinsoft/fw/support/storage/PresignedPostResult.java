package com.dobbinsoft.fw.support.storage;

import com.dobbinsoft.fw.core.annotation.doc.ApiField;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedPostResult {

    private String policy;

    private String signature;

    private String bucket;

    private String key;

    private String accessKeyId;

    @ApiField(description = "POST上传链接")
    private String url;

}
