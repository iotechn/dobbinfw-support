package com.dobbinsoft.fw.support.rpc;

import com.dobbinsoft.fw.core.exception.ServiceException;

public interface RpcContextCommonProvider {

    // 在RPC时，在执行前可以做一些事情，例如将调用上下文设置到ThreadLocal里面
    public void provide() throws ServiceException;

}
