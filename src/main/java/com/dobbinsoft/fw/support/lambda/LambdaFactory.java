package com.dobbinsoft.fw.support.lambda;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.core.annotation.HttpMethod;
import com.dobbinsoft.fw.core.annotation.HttpParam;
import com.dobbinsoft.fw.core.annotation.RpcService;
import com.dobbinsoft.fw.core.annotation.param.NotNull;
import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwRpcConsumerProperties;
import com.dobbinsoft.fw.support.properties.FwRpcProviderProperties;
import com.dobbinsoft.fw.support.rpc.RpcContextHolder;
import com.dobbinsoft.fw.support.utils.JwtUtils;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

@Component
public class LambdaFactory {

    @Autowired
    private FwRpcProviderProperties fwRpcProviderProperties;

    @Autowired
    private FwRpcConsumerProperties fwRpcConsumerProperties;


    /**
     * 从一个接口定义,快速创建LambdaDTO
     * @param method
     * @return
     */
    public LambdaDTO from(Method method, String baseUrl, int jwtExpireSeconds) throws ServiceException {
        // 调用方SystemID
        String systemId = fwRpcConsumerProperties.getSystemId();
        if (StringUtils.isEmpty(systemId)) {
            throw new ServiceException(CoreExceptionDefinition.LAMBDA_LEAK_PROPERTIES);
        }
        boolean anySystemId = false;
        List<FwRpcProviderProperties.RpcConsumer> consumers = fwRpcProviderProperties.getConsumers();
        for (FwRpcProviderProperties.RpcConsumer consumer : consumers) {
            if (consumer.getSystemId().equals(systemId)) {
                anySystemId = true;
                break;
            }
        }
        if (!anySystemId) {
            // 以自己私钥的签名调用自己
            throw new ServiceException(CoreExceptionDefinition.LAMBDA_LEAK_PROPERTIES);
        }
        HttpMethod httpMethod = method.getAnnotation(HttpMethod.class);
        if (httpMethod == null) {
            throw new ServiceException(CoreExceptionDefinition.LAMBDA_NO_ANNOTATION);
        }
        RpcService rpcService = method.getDeclaringClass().getAnnotation(RpcService.class);
        if (rpcService == null) {
            throw new ServiceException(CoreExceptionDefinition.LAMBDA_NO_ANNOTATION);
        }

        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put("systemId", fwRpcConsumerProperties.getSystemId());
        String rsa256 = null;
        try {
            rsa256 = JwtUtils.createRSA256(jwtHeader, Collections.emptyMap(), jwtExpireSeconds, fwRpcConsumerProperties.getPrivateKey());
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        LambdaDTO lambdaDTO = new LambdaDTO();
        lambdaDTO.setUrl(baseUrl + "/" + rpcService.group() + "/" + method.getName());
        lambdaDTO.setDescription(httpMethod.description());

        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put(Const.RPC_SYSTEM_ID, fwRpcConsumerProperties.getSystemId());
        httpHeaders.put(Const.RPC_HEADER, rsa256);
        Parameter[] parameters = method.getParameters();
        Map<String, LambdaDTO.LambdaParamDTO> lambdaParamMap = new HashMap<>();
        for (Parameter parameter : parameters) {
            HttpParam httpParam = parameter.getAnnotation(HttpParam.class);
            if (httpParam != null) {
                Class<?> parameterType = parameter.getType();
                LambdaParamType lambdaParamType = null;
                if (parameterType == String.class) {
                    lambdaParamType = LambdaParamType.string;
                } else if (parameterType == Integer.class || parameter.getType() == Long.class) {
                    lambdaParamType = LambdaParamType.integer;
                } else if (parameterType == BigDecimal.class || parameterType == Double.class || parameterType == Long.class) {
                    lambdaParamType = LambdaParamType.number;
                } else if (List.class.isAssignableFrom(parameterType) || Set.class.isAssignableFrom(parameterType)) {
                    lambdaParamType = LambdaParamType.array;
                } else {
                    throw new ServiceException(CoreExceptionDefinition.LAMBDA_PARAM_JUST_BASIC);
                }
                LambdaDTO.LambdaParamDTO lambdaParamDTO = new LambdaDTO.LambdaParamDTO();
                lambdaParamDTO.setName(httpParam.name());
                lambdaParamDTO.setType(lambdaParamType);
                lambdaParamDTO.setDescription(httpParam.description());
                lambdaParamDTO.setRequired(parameter.getAnnotation(NotNull.class) != null);
                lambdaParamMap.put(httpParam.name(), lambdaParamDTO);
            }
        }
        lambdaDTO.setParams(lambdaParamMap);
        lambdaDTO.setHeaders(httpHeaders);
        return lambdaDTO;
    }

}
