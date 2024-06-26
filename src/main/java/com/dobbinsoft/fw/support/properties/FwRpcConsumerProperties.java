package com.dobbinsoft.fw.support.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.dobbinsoft.rpc-consumer")
public class FwRpcConsumerProperties {

    /**
     * 消费者私钥，用于鉴权
     */
    private String privateKey;

    /**
     * 作为消费者，系统唯一ID
     */
    private String systemId;

    /**
     * 我们要调用哪些远程方法？
     */
    private List<RpcProvider> providers;

    @Getter
    @Setter
    public static class RpcProvider {

        /**
         * 远程方法的SystemID
         */
        private String systemId;

        /**
         * 远程方法的url
         */
        private String url;

    }

}
