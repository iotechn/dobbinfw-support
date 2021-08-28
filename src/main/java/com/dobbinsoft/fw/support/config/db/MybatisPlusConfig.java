package com.dobbinsoft.fw.support.config.db;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(value = {MybatisPlusProperties.class})
public class MybatisPlusConfig {

    /**
     * 新的分页插件,一缓和二缓遵循mybatis的规则
     */
    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        return new PaginationInnerInterceptor(DbType.MYSQL);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> innerInterceptorList) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        for (InnerInterceptor innerInterceptor : innerInterceptorList) {
            interceptor.addInnerInterceptor(innerInterceptor);
        }
        return interceptor;
    }

    @Bean
    public FwSqlInjector fwSqlInjector() {
        return new FwSqlInjector();
    }

    @Bean
    public FwMetaObjectHandler fwMetaObjectHandler() {
        return new FwMetaObjectHandler();
    }
}
