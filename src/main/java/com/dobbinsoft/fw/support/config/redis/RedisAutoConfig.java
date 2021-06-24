package com.dobbinsoft.fw.support.config.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@Configuration
public class RedisAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisAutoConfig.class);

    /**** 缓存专用数据源 ****/
    @Bean
    public LettuceConnectionFactory defaultLettuceConnectionFactory(
            RedisConfiguration defaultRedisConfig,GenericObjectPoolConfig defaultPoolConfig) {
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(5000))
                        .poolConfig(defaultPoolConfig).build();
        return new LettuceConnectionFactory(defaultRedisConfig, clientConfig);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(
            LettuceConnectionFactory defaultLettuceConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(defaultLettuceConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory defaultLettuceConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(defaultLettuceConnectionFactory);
        return stringRedisTemplate;
    }

    /**** 用户SESSION专用数据源 ****/
    @Bean
    public LettuceConnectionFactory userLettuceConnectionFactory(
            RedisConfiguration userRedisConfig,GenericObjectPoolConfig userPoolConfig) {
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(5000))
                        .poolConfig(userPoolConfig).build();
        return new LettuceConnectionFactory(userRedisConfig, clientConfig);
    }


    @Bean
    public StringRedisTemplate userRedisTemplate(LettuceConnectionFactory userLettuceConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(userLettuceConnectionFactory);
        return redisTemplate;
    }



    /**** 锁专用数据源 ****/
    @Bean
    public LettuceConnectionFactory lockLettuceConnectionFactory(
            RedisConfiguration lockRedisConfig,GenericObjectPoolConfig lockPoolConfig) {
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(5000))
                        .poolConfig(lockPoolConfig).build();
        return new LettuceConnectionFactory(lockRedisConfig, clientConfig);
    }

    @Bean
    public StringRedisTemplate lockRedisTemplate(LettuceConnectionFactory lockLettuceConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(lockLettuceConnectionFactory);
        return redisTemplate;
    }


    @Configuration
    public static class UserRedisConfig {
        @Value("${spring.redis.mode}")
        private String mode;
        @Value("${spring.redis.master-name}")
        private String masterName;
        @Value("${spring.user-redis.host:127.0.0.1:6379}")
        private String host;
        @Value("${spring.user-redis.password:}")
        private String password;
        @Value("${spring.user-redis.database:0}")
        private Integer database;

        @Value("${spring.user-redis.lettuce.pool.max-active:8}")
        private Integer maxActive;
        @Value("${spring.user-redis.lettuce.pool.max-idle:8}")
        private Integer maxIdle;
        @Value("${spring.user-redis.lettuce.pool.max-wait:-1}")
        private Long maxWait;
        @Value("${spring.user-redis.lettuce.pool.min-idle:0}")
        private Integer minIdle;

        @Bean
        public GenericObjectPoolConfig userPoolConfig() {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(maxWait);
            return config;
        }

        @Bean
        public RedisConfiguration userRedisConfig() {
            return getRedisConfiguration(masterName, mode, host, password, database);
        }
    }

    @Configuration
    public static class LockRedisConfig {
        @Value("${spring.redis.master-name}")
        private String masterName;
        @Value("${spring.redis.mode}")
        private String mode;
        @Value("${spring.lock-redis.host:127.0.0.1:6379}")
        private String host;
        @Value("${spring.lock-redis.password:}")
        private String password;
        @Value("${spring.lock-redis.database:0}")
        private Integer database;

        @Value("${spring.lock-redis.lettuce.pool.max-active:8}")
        private Integer maxActive;
        @Value("${spring.lock-redis.lettuce.pool.max-idle:8}")
        private Integer maxIdle;
        @Value("${spring.lock-redis.lettuce.pool.max-wait:-1}")
        private Long maxWait;
        @Value("${spring.lock-redis.lettuce.pool.min-idle:0}")
        private Integer minIdle;

        @Bean
        public GenericObjectPoolConfig lockPoolConfig() {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(maxWait);
            return config;
        }

        @Bean
        public RedisConfiguration lockRedisConfig() {
            return getRedisConfiguration(masterName, mode, host, password, database);
        }
    }


    @Configuration
    public static class DefaultRedisConfig {
        @Value("${spring.redis.master-name}")
        private String masterName;
        @Value("${spring.redis.mode}")
        private String mode;
        @Value("${spring.redis.host:127.0.0.1:6379}")
        private String host;
        @Value("${spring.redis.password:}")
        private String password;
        @Value("${spring.redis.database:0}")
        private Integer database;

        @Value("${spring.redis.lettuce.pool.max-active:8}")
        private Integer maxActive;
        @Value("${spring.redis.lettuce.pool.max-idle:8}")
        private Integer maxIdle;
        @Value("${spring.redis.lettuce.pool.max-wait:-1}")
        private Long maxWait;
        @Value("${spring.redis.lettuce.pool.min-idle:0}")
        private Integer minIdle;

        @Bean
        public GenericObjectPoolConfig defaultPoolConfig() {
            GenericObjectPoolConfig config = new GenericObjectPoolConfig();
            config.setMaxTotal(maxActive);
            config.setMaxIdle(maxIdle);
            config.setMinIdle(minIdle);
            config.setMaxWaitMillis(maxWait);
            return config;
        }

        @Bean
        public RedisConfiguration defaultRedisConfig() {
            return getRedisConfiguration(masterName, mode, host, password, database);
        }

    }

    private static RedisConfiguration getRedisConfiguration(String masterName, String mode, String host, String password, Integer database) {
        if (mode.equals("single")) {
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            String[] hostArray = host.split(":");
            config.setHostName(hostArray[0]);
            config.setPassword(RedisPassword.of(password));
            config.setPort(Integer.parseInt(hostArray[1]));
            config.setDatabase(database);
            return config;
        } else if (mode.equals("sentinel")) {
            RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
            configuration.setMaster(masterName);
            String[] hostList = host.split(",");
            List<RedisNode> serverList = new LinkedList<>();
            for (String hostItem : hostList) {
                String[] hostArray = hostItem.split(":");
                RedisServer redisServer = new RedisServer(hostArray[0], Integer.parseInt(hostArray[1]));
                serverList.add(redisServer);
            }
            configuration.setSentinels(serverList);
            logger.info("[Redis] 哨兵节点: masterName={}, host={}", masterName, host);
            return configuration;
        } else {
            return null;
        }
    }

}