package com.dobbinsoft.fw.core.annotation.param.transfer;

public interface CustomFieldTransfer<F, T> {

    public T transfer(F f);

    public F recover(T t);

    public Class<?> annotation();

}
