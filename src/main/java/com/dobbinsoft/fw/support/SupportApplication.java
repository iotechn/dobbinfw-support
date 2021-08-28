//package com.dobbinsoft.fw.support;
//
//import com.dobbinsoft.fw.support.annotation.cache.CacheProxy;
//import com.dobbinsoft.fw.support.domain.SuperDO;
//import com.dobbinsoft.fw.support.mapper.test.TestMapper;
//import org.mybatis.spring.annotation.MapperScan;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.stereotype.Controller;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Date;
//
//@SpringBootApplication(
//        scanBasePackages = {
//                "com.dobbinsoft.fw"
//        },
//        exclude = {
//                RedisAutoConfiguration.class,
//                RedisReactiveAutoConfiguration.class
//        })
//@MapperScan({
//        "com.dobbinsoft.fw.support.mapper.test"
//})
//@Controller
//public class SupportApplication {
//
//    private static final Logger logger = LoggerFactory.getLogger(SupportApplication.class);
//
//    public static void main(String[] args) {
//        ConfigurableApplicationContext applicationContext = SpringApplication.run(SupportApplication.class, args);
//        SupportApplication bean = applicationContext.getBean(SupportApplication.class);
//        bean.testRead();
//    }
//
//    @Autowired
//    private TestMapper testMapper;
//
//    @CacheProxy
//    @Transactional(rollbackFor = Exception.class)
//    public Boolean testWrite() {
//        SuperDO superDO = new SuperDO();
//        superDO.setId(123L);
//        superDO.setGmtCreate(new Date());
//        superDO.setGmtUpdate(new Date());
//        testMapper.insert(superDO);
//        logger.info("[write comp]");
//        return true;
//    }
//
//    @CacheProxy
//    @Transactional(rollbackFor = Exception.class)
//    public Boolean testRead() {
//        SuperDO superDO = testMapper.selectById(5L);
//        System.out.printf("");
//        return true;
//    }
//
//}
