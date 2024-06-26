package com.dobbinsoft.fw.support.rpc;


import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.core.annotation.HttpParam;
import com.dobbinsoft.fw.core.annotation.RpcService;
import com.dobbinsoft.fw.core.exception.ServiceException;
import com.dobbinsoft.fw.support.properties.FwRpcConsumerProperties;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.JwtUtils;
import com.dobbinsoft.fw.support.utils.TimeUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RpcConsumerProxy implements InitializingBean {

    @Autowired
    private FwRpcConsumerProperties fwRpcConsumerProperties;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private final Map<String, FwRpcConsumerProperties.RpcProvider> providerMap = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<FwRpcConsumerProperties.RpcProvider> providers = fwRpcConsumerProperties.getProviders();
        if (CollectionUtils.isNotEmpty(providers)) {
            for (FwRpcConsumerProperties.RpcProvider provider : providers) {
                providerMap.put(provider.getSystemId(), provider);
            }
        }
    }

    public <T> T getInstance(Class<T> interfaceClass) {
        RpcService rpcService = interfaceClass.getAnnotation(RpcService.class);
        if (rpcService == null) {
            throw new RuntimeException("[RPC消费者代理] 创建失败，未注解@RpcService: %s".formatted(interfaceClass.getName()));
        }
        Object o = Proxy.newProxyInstance(RpcConsumerProxy.class.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            try {
                String methodName = method.getName();
                FormBody.Builder builder = new FormBody.Builder();
                builder.add("_gp", rpcService.group());
                builder.add("_mt", methodName);
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    HttpParam httpParam = parameter.getAnnotation(HttpParam.class);
                    if (httpParam == null) {
                        throw new RuntimeException("[RPC消费者代理] 调用失败，未注解@HttpParam, class: %s; parameter: %s".formatted(interfaceClass.getName(), parameter.getName()));
                    }
                    // 通过数据类型格式化未不同的
                    if (args[i] != null) {
                        Class<?> type = args[i].getClass();
                        if (Const.IGNORE_PARAM_LIST.contains(type)) {
                            builder.add(httpParam.name(), args[i].toString());
                        } else if (type == LocalDateTime.class) {
                            builder.add(httpParam.name(), TimeUtils.localDateTimeToString((LocalDateTime) args[i]));
                        } else if (type == LocalDate.class) {
                            builder.add(httpParam.name(), TimeUtils.localDateToString((LocalDate) args[i]));
                        } else if (type == LocalTime.class) {
                            builder.add(httpParam.name(), TimeUtils.localTimeToString((LocalTime) args[i]));
                        } else if (type == Date.class) {
                            builder.add(httpParam.name(), TimeUtils.dateToString((Date) args[i]));
                        } else if (type == BigDecimal.class) {
                            builder.add(httpParam.name(), args[i].toString());
                        } else {
                            builder.add(httpParam.name(), JacksonUtil.toJSONString(args[i]));
                        }
                    }
                }
                FwRpcConsumerProperties.RpcProvider rpcProvider = providerMap.get(rpcService.systemId());
                if (rpcProvider == null) {
                    throw new RuntimeException("[RPC消费者代理] 调用失败，SystemID未配置: %s".formatted(rpcProvider.getSystemId()));
                }
                Map<String, String> header = new HashMap<>();
                header.put("systemId", fwRpcConsumerProperties.getSystemId());
                Map<String, String> payload = RpcContextHolder.getAll();
                String rsa256 = JwtUtils.createRSA256(header, payload, 120, fwRpcConsumerProperties.getPrivateKey());
                String json = okHttpClient.newCall(
                                new Request
                                        .Builder()
                                        .url(rpcProvider.getUrl())
                                        .header(Const.RPC_HEADER, rsa256)
                                        .header(Const.RPC_SYSTEM_ID, fwRpcConsumerProperties.getSystemId())
                                        .post(builder.build())
                                        .build())
                        .execute()
                        .body()
                        .string();
                Class<?> returnType = method.getReturnType();
                Type genericReturnType = method.getGenericReturnType();
                JsonNode jsonNode = JacksonUtil.parseObject(json);
                assert jsonNode != null;
                if (jsonNode.get("errno").asInt() == 200) {
                    if (genericReturnType instanceof ParameterizedType) {
                        TypeFactory typeFactory = JacksonUtil.objectMapper.getTypeFactory();
                        JavaType javaType = typeFactory.constructType(returnType);
                        // 使用JavaType进行反序列化
                        return JacksonUtil.objectMapper.convertValue(jsonNode.get("data"), javaType);
                    } else if (Const.IGNORE_PARAM_LIST.contains(returnType)) {
                        Constructor<?> constructor = returnType.getConstructor(String.class);
                        return constructor.newInstance(jsonNode.get("data").asText());
                    } else if (returnType == LocalDateTime.class) {
                        return TimeUtils.stringToLocalDateTime(jsonNode.get("data").asText());
                    } else if (returnType == LocalDate.class) {
                        return TimeUtils.stringToLocalDate(jsonNode.get("data").asText());
                    } else if (returnType == LocalTime.class) {
                        return TimeUtils.stringToLocalTime(jsonNode.get("data").asText());
                    } else if (returnType == Date.class) {
                        return TimeUtils.stringToDate(jsonNode.get("data").asText());
                    } else if (returnType == BigDecimal.class) {
                        return new BigDecimal(jsonNode.get("data").asText());
                    } else {
                        return JacksonUtil.objectMapper.convertValue(jsonNode.get("data"), returnType);
                    }
                }
                log.error("[RPC消费者代理] 服务异常：group: {}; method: {}, error message: {}", rpcService.group(), methodName, jsonNode.get("errmsg").asText());
                throw new ServiceException(jsonNode.get("errmsg").asText(), jsonNode.get("errno").asInt());
            } finally {
                RpcContextHolder.clear();
            }
        });
        return (T) o;
    }

}
