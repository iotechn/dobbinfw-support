package com.dobbinsoft.fw.support.sms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class QCloudSendSMSRequest {

    @JsonProperty("PhoneNumberSet")
    private List<String> phoneNumberSet;
    @JsonProperty("SmsSdkAppId")
    private String smsSdkAppId;
    @JsonProperty("SignName")
    private String signName;
    @JsonProperty("TemplateId")
    private String templateId;
    @JsonProperty("TemplateParamSet")
    private List<String> templateParamSet;
    @JsonProperty("SessionContext")
    private String sessionContext;
}
