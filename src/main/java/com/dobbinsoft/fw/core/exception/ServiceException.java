package com.dobbinsoft.fw.core.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: rize
 * Date: 2019-01-31
 * Time: 下午8:07
 */
@Getter
public class ServiceException extends Exception implements Serializable {

    @Setter
    private int code;

    private Object attach;

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
        this.code = CoreExceptionDefinition.PARAM_CHECK_FAILED.getCode();
    }

    public ServiceException(String message, int code) {
        super(message);
        this.code = code;
    }



    public ServiceException(ServiceExceptionDefinition definition) {
        super(definition.getMsg());
        this.code = definition.getCode();
    }

    public ServiceException attach(Object attach) {
        this.attach = attach;
        return this;
    }
}
