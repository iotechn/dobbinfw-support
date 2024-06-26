package com.dobbinsoft.fw.support.annotation;

import com.dobbinsoft.fw.support.config.rpc.RpcConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * ClassName: EnableRpc
 * Description: 激活RPC
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(RpcConfig.class)
@Documented
public @interface EnableRpc {

    /**
     * 需要动态代理的RpcService接口
     * @return
     */
    public Class[] providerApis();

}
