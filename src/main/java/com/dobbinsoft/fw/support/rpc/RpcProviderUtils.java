package com.dobbinsoft.fw.support.rpc;

import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwRpcProviderProperties;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.JwtUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RpcProviderUtils implements InitializingBean {

    @Autowired
    private FwRpcProviderProperties fwRpcProviderProperties;

    private Map<String, FwRpcProviderProperties.RpcConsumer> consumerMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<FwRpcProviderProperties.RpcConsumer> consumers = fwRpcProviderProperties.getConsumers();
        if (CollectionUtils.isNotEmpty(consumers)) {
            for (FwRpcProviderProperties.RpcConsumer consumer : consumers) {
                consumerMap.put(consumer.getSystemId(), consumer);
            }
        }
    }

    /**
     * 校验Token是否正确
     * @param systemId
     * @param token
     * @return
     * @throws ServiceException
     */
    public JwtUtils.JwtResult validToken(String systemId, String token) throws ServiceException {
        if (StringUtils.isEmpty(systemId) || StringUtils.isEmpty(token)) {
            throw new ServiceException(CoreExceptionDefinition.LAUNCHER_RPC_SIGN_INCORRECT);
        }
        FwRpcProviderProperties.RpcConsumer rpcConsumer = consumerMap.get(systemId);
        if (rpcConsumer == null) {
            throw new ServiceException(CoreExceptionDefinition.LAUNCHER_RPC_SIGN_INCORRECT);
        }
        return JwtUtils.verifyRSA256(token, rpcConsumer.getPublicKey(), JacksonUtil.objectMapper);
    }
}
