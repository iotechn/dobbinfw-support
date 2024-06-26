package com.dobbinsoft.fw.support.properties;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.rpc-provider")
public class FwRpcProviderProperties {

    /**
     * 有哪些客户端会调用我们？
     */
    private List<RpcConsumer> consumers;

    @Getter
    @Setter
    public static class RpcConsumer {

        /**
         * 调用方唯一系统ID
         */
        private String systemId;

        /**
         * 调用方公钥
         */
        private String publicKey;

        /**
         * 允许访问的API（暂时不支持，后面业务有需要再支持）
         */
//      private List<String> groups;

    }


}
