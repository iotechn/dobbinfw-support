package com.dobbinsoft.fw.support.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WsWrapper {

    private String identityOwnerKey;

    private WsWrapper() {}

    public static WsWrapper build(String identityOwnerKey) {
        WsWrapper wsWrapper = new WsWrapper();
        wsWrapper.setIdentityOwnerKey(identityOwnerKey);
        return wsWrapper;
    }

}
