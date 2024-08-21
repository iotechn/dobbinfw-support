package com.dobbinsoft.fw.support.config.rpc;

import com.dobbinsoft.fw.support.rpc.RpcConsumerProxy;
import com.dobbinsoft.fw.support.rpc.RpcProviderUtils;
import org.springframework.context.annotation.Bean;

public class RpcConfig {

    @Bean
    public RpcProviderUtils rpcProviderUtils() {
        return new RpcProviderUtils();
    }

    @Bean
    public RpcConsumerProxy rpcConsumerProxy() {
        return new RpcConsumerProxy();
    }

}
