package com.dobbinsoft.fw.support.session;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionWrapper {

    private String identityOwnerJson;

    private String token;

}
