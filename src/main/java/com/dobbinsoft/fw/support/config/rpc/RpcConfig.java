package com.dobbinsoft.fw.support.config.rpc;

import com.dobbinsoft.fw.support.annotation.EnableRpc;
import com.dobbinsoft.fw.support.rpc.RpcConsumerProxy;
import com.dobbinsoft.fw.support.rpc.RpcProviderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

public class RpcConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public RpcProviderUtils rpcProviderUtils() {
        return new RpcProviderUtils();
    }

    @Bean
    public RpcConsumerProxy rpcConsumerProxy() {
        return new RpcConsumerProxy();
    }

    @Bean
    public List<Object> rpcInterfaces(RpcConsumerProxy rpcConsumerProxy) {
        // 获取带有@SpringBootApplication注解的bean
        Object mainBean = applicationContext.getBeansWithAnnotation(SpringBootApplication.class)
                .values().iterator().next();

        // 获取原始类
        Class<?> mainClass = getOriginalClass(mainBean.getClass());

        List<Object> providers = new ArrayList<>();
        // 检查启动类上是否有EnableRpc注解
        if (mainClass.isAnnotationPresent(EnableRpc.class)) {
            EnableRpc enableRpc = mainClass.getAnnotation(EnableRpc.class);
            // 获取providerApis属性值
            Class<?>[] providerApis = enableRpc.providerApis();
            for (Class<?> providerApi : providerApis) {
                providers.add(createRpcBean(rpcConsumerProxy, providerApi));
            }
        }
        return providers;
    }

    private Object createRpcBean(RpcConsumerProxy rpcConsumerProxy, Class<?> providerApi) {
        Object instance = rpcConsumerProxy.getInstance(providerApi);
        // 注册bean到Spring容器中
        ((DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory())
                .registerSingleton(providerApi.getName(), instance);
        return instance;
    }

    private Class<?> getOriginalClass(Class<?> clazz) {
        // 如果是CGLIB代理类，则获取其父类
        if (clazz.getName().contains("$$")) {
            return clazz.getSuperclass();
        }
        return clazz;
    }

}
