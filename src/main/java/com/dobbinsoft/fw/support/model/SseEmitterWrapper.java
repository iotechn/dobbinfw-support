package com.dobbinsoft.fw.support.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Getter
@Setter
public class SseEmitterWrapper {

    private String identityOwnerKey;

    private SseEmitter sseEmitter;

}
