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
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcConsumerProxy implements InitializingBean {

    @Autowired
    private FwRpcConsumerProperties fwRpcConsumerProperties;

    @Autowired(required = false)
    private RpcContextCommonProvider rpcContextCommonProvider;

    private final WebClient webClient = WebClient.create();

    private final Map<String, FwRpcConsumerProperties.RpcProvider> providerMap = new HashMap<>();

    private static final int JWT_EXPIRE_SECONDS = 60 * 60;

    // Key 为 SystemID
    private final Cache<String, String> jwtCache = Caffeine.newBuilder()
            .expireAfterWrite(JWT_EXPIRE_SECONDS * 4 / 5, TimeUnit.SECONDS)
            .build();

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
        return getInstance(interfaceClass, true);
    }

    /**
     *
     * @param interfaceClass 接口Class
     * @param sync 是否异步，若为true，Object为Mono， 否则直接返回对象
     * @return
     * @param <T>
     */
    public <T> T getInstance(Class<T> interfaceClass, boolean sync) {
        RpcService rpcService = interfaceClass.getAnnotation(RpcService.class);
        if (rpcService == null) {
            throw new RuntimeException("[RPC消费者代理] 创建失败，未注解@RpcService: %s".formatted(interfaceClass.getName()));
        }
        Object o = Proxy.newProxyInstance(RpcConsumerProxy.class.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            String methodName = method.getName();
            LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap();
            formData.add("_gp", rpcService.group());
            formData.add("_mt", methodName);
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
                    String value;
                    if (Const.IGNORE_PARAM_LIST.contains(type)) {
                        value = args[i].toString();
                    } else if (type == LocalDateTime.class) {
                        value = TimeUtils.localDateTimeToString((LocalDateTime) args[i]);
                    } else if (type == LocalDate.class) {
                        value = TimeUtils.localDateToString((LocalDate) args[i]);
                    } else if (type == LocalTime.class) {
                        value = TimeUtils.localTimeToString((LocalTime) args[i]);
                    } else if (type == Date.class) {
                        value = TimeUtils.dateToString((Date) args[i]);
                    } else if (type == BigDecimal.class) {
                        value = args[i].toString();
                    } else {
                        value = JacksonUtil.toJSONString(args[i]);
                    }
                    formData.add(httpParam.name(), value);
                }
            }
            FwRpcConsumerProperties.RpcProvider rpcProvider = providerMap.get(rpcService.systemId());
            if (rpcProvider == null) {
                throw new ServiceException("[RPC消费者代理] 调用失败，SystemID未配置: %s".formatted(rpcService.systemId()));
            }
            Map<String, Object> header = new HashMap<>();
            header.put("systemId", fwRpcConsumerProperties.getSystemId());
            // 此操作会将通用context写入到context上下文中
            if (rpcContextCommonProvider != null) {
                rpcContextCommonProvider.provide();
            }
            Map<String, String> context = RpcContextHolder.getAll();
            String rsa256 = jwtCache.getIfPresent(fwRpcConsumerProperties.getSystemId());
            if (rsa256 == null) {
                rsa256 = JwtUtils.createRSA256(header, Collections.emptyMap(), JWT_EXPIRE_SECONDS, fwRpcConsumerProperties.getPrivateKey());
                jwtCache.put(fwRpcConsumerProperties.getSystemId(), rsa256);
            }

            Class<?> returnType = method.getReturnType();
            Type genericReturnType = method.getGenericReturnType();
            if (returnType == Mono.class && genericReturnType instanceof ParameterizedType) {
                // 使用泛型
                returnType = (Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                genericReturnType = returnType;
            }
            Class<?> finalReturnType = returnType;
            Type finalGenericReturnType = genericReturnType;
            return webClient
                    .post()
                    .uri(rpcProvider.getUrl())
                    .header(Const.RPC_HEADER, rsa256)
                    .header(Const.RPC_CONTEXT_JSON, JacksonUtil.toJSONString(context))
                    .header(Const.RPC_SYSTEM_ID, fwRpcConsumerProperties.getSystemId())
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .flatMap(jsonNode -> {
                        try {
                            // 无法解决流式请求的问题，目前也没有此应用场景
                            if (jsonNode.get("errno").asInt() == 200) {
                                if (finalGenericReturnType instanceof ParameterizedType) {
                                    TypeFactory typeFactory = JacksonUtil.objectMapper.getTypeFactory();
                                    JavaType javaType = typeFactory.constructType(finalGenericReturnType);
                                    // 使用JavaType进行反序列化
                                    return Mono.just(JacksonUtil.objectMapper.convertValue(jsonNode.get("data"), javaType));
                                } else if (Const.IGNORE_PARAM_LIST.contains(finalReturnType)) {
                                    Constructor<?> constructor = finalReturnType.getConstructor(String.class);
                                    return Mono.just(constructor.newInstance(jsonNode.get("data").asText()));
                                } else if (finalReturnType == LocalDateTime.class) {
                                    return Mono.just(TimeUtils.stringToLocalDateTime(jsonNode.get("data").asText()));
                                } else if (finalReturnType == LocalDate.class) {
                                    return Mono.just(TimeUtils.stringToLocalDate(jsonNode.get("data").asText()));
                                } else if (finalReturnType == LocalTime.class) {
                                    return Mono.just(TimeUtils.stringToLocalTime(jsonNode.get("data").asText()));
                                } else if (finalReturnType == Date.class) {
                                    return Mono.just(TimeUtils.stringToDate(jsonNode.get("data").asText()));
                                } else if (finalReturnType == BigDecimal.class) {
                                    return Mono.just(new BigDecimal(jsonNode.get("data").asText()));
                                } else {
                                    JsonNode data = jsonNode.get("data");
                                    if (data == null || data instanceof NullNode) {
                                        return Mono.empty();
                                    }
                                    return Mono.just(JacksonUtil.objectMapper.convertValue(data, finalReturnType));
                                }
                            }
                            log.error("[RPC消费者代理] 服务异常：group: {}; method: {}, error message: {}", rpcService.group(), methodName, jsonNode.get("errmsg").asText());
                            return Mono.error(new ServiceException(jsonNode.get("errmsg").asText(), jsonNode.get("errno").asInt()));
                        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                                 InvocationTargetException e) {
                            return Mono.error(new ServiceException("RPC反射异常 systemId:%s".formatted(rpcService.systemId())));
                        }
                    })
                    .doOnError(e -> {
                        if (e instanceof ServiceException se) {
                            log.error("[RPC消费者代理] 服务异常：group: {}; method: {}, error message: {}",
                                    rpcService.group(), methodName, se.getMessage());
                        } else {
                            log.error("[RPC消费者代理] RPC调用异常: {}", e.getMessage(), e);
                        }
                    })
                    .doFinally(signalType -> {
                        RpcContextHolder.clear();
                    });
        });
        return (T) o;
    }

}
