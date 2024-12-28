package com.dobbinsoft.fw.support.lambda;

import com.dobbinsoft.fw.core.enums.BaseEnums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LambdaParamType implements BaseEnums<String> {
    string("string"),
    integer("integer"),
    number("number"),
    bool("boolean"),
    array("array")
    ;

    private final String code;

    @Override
    public String getMsg() {
        return this.name();
    }
}
