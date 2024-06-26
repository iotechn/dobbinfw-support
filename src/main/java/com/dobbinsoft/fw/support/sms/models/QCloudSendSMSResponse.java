package com.dobbinsoft.fw.support.sms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class QCloudSendSMSResponse {

    @JsonProperty("Response")
    private ResponseDTO response;

    @NoArgsConstructor
    @Data
    public static class ResponseDTO {
        @JsonProperty("SendStatusSet")
        private List<SendStatusSetDTO> sendStatusSet;
        @JsonProperty("RequestId")
        private String requestId;

        @NoArgsConstructor
        @Data
        public static class SendStatusSetDTO {
            @JsonProperty("SerialNo")
            private String serialNo;
            @JsonProperty("PhoneNumber")
            private String phoneNumber;
            @JsonProperty("Fee")
            private Integer fee;
            @JsonProperty("SessionContext")
            private String sessionContext;
            @JsonProperty("Code")
            private String code;
            @JsonProperty("Message")
            private String message;
            @JsonProperty("IsoCode")
            private String isoCode;
        }
    }
}
