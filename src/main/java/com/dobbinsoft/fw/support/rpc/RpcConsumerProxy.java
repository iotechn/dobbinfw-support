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
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Duration;
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

    private final OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .callTimeout(Duration.ofSeconds(60 * 3))
            .readTimeout(Duration.ofSeconds(60 * 3))
            .build();

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
                if (returnType == Flux.class) {
                    // 流式调用
                    // 执行请求并获取响应
                    String finalRsa25 = rsa256;
//                    Type genericReturnType = method.getGenericReturnType();
                    return Flux.create(sink -> {
                        // 构建请求
                        Request request = new Request.Builder()
                                .url(rpcProvider.getUrl() + "-sse" + "/" + rpcService.group() + "/" + methodName)
                                .header(Const.RPC_HEADER, finalRsa25)
                                .header(Const.RPC_CONTEXT_JSON, JacksonUtil.toJSONString(context))
                                .header(Const.RPC_SYSTEM_ID, fwRpcConsumerProperties.getSystemId())
                                .post(builder.build())
                                .build();

                        // 异步调用 API
                        okHttpClient.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                // 如果请求失败，传递错误
                                sink.error(e);
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                if (response.isSuccessful() && response.body() != null) {
                                    BufferedSource source = response.body().source();
                                    try {
                                        // 逐行读取数据并发出
                                        while (!source.exhausted()) {
                                            String line = source.readUtf8Line();
                                            if (line != null) {
                                                if (line.startsWith("data:")) {
                                                    String eventData = line.substring(5).trim(); // 获取事件数据
                                                    // 每次读取到一行数据时，发出事件
                                                    sink.next(eventData);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        sink.error(e); // 错误处理
                                    } finally {
                                        sink.complete(); // 流结束，调用 complete
                                    }
                                } else {
                                    // 如果响应不成功，发出错误
                                    sink.error(new IOException("Failed to connect, response code: " + response.code()));
                                }
                            }
                        });
                    });

                } else {
                    String json = okHttpClient.newCall(
                                    new Request
                                            .Builder()
                                            .url(rpcProvider.getUrl() + "/" + rpcService.group() + "/" + methodName)
                                            .header(Const.RPC_HEADER, rsa256)
                                            .header(Const.RPC_CONTEXT_JSON, JacksonUtil.toJSONString(context))
                                            .header(Const.RPC_SYSTEM_ID, fwRpcConsumerProperties.getSystemId())
                                            .post(builder.build())
                                            .build())
                            .execute()
                            .body()
                            .string();
                    Type genericReturnType = method.getGenericReturnType();
                    JsonNode jsonNode = JacksonUtil.parseObject(json);
                    assert jsonNode != null;
                    if (jsonNode.get("errno").asInt() == 200) {
                        if (genericReturnType instanceof ParameterizedType) {
                            TypeFactory typeFactory = JacksonUtil.objectMapper.getTypeFactory();
                            JavaType javaType = typeFactory.constructType(genericReturnType);
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
                }
            } catch (IOException e) {
                log.error("[RPC消费者代理] 上游网络不通, msg: {}",e.getMessage());
                throw new ServiceException("RPC网络异常 systemId:%s".formatted(rpcService.systemId()));
            } finally {
                RpcContextHolder.clear();
            }
        });
        return (T) o;
    }

}
