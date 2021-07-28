package com.dobbinsoft.fw.support.image;

import lombok.Data;

@Data
public class ImageModel {

    private String title;

    private String url;

    private Integer bizType;

    private String bizTypeTitle;

    private Long bizId;

    private Long contentLength;

}
