package com.dobbinsoft.fw.core.annotation.param;

import com.dobbinsoft.fw.core.exception.ServiceException;

/**
 * ClassName: Validator
 * Description: 校验器
 * @param <T> 数据类型
 */
public interface CustomValidator<T> {

    /**
     * 自定义校验器，可在内部直接抛出异常。也可以返回false
     * @param param
     * @return
     * @throws ServiceException
     */
    public boolean valid(T param) throws ServiceException;

}
