package com.dobbinsoft.fw.support.sms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AliSendSMSResult {

    @JsonProperty("Message")
    private String message;
    @JsonProperty("RequestId")
    private String requestId;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("BizId")
    private String bizId;
}
