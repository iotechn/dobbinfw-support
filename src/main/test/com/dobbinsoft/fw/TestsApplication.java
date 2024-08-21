package com.dobbinsoft.fw;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootApplication(
        scanBasePackages = {
                "com.dobbinsoft.fw"
        },
        exclude = {
                RedisAutoConfiguration.class,
                RedisReactiveAutoConfiguration.class
        })
@Slf4j
public class TestsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestsApplication.class, args);
        log.info("[Support Test IoC] 初始化成功");
    }


}
