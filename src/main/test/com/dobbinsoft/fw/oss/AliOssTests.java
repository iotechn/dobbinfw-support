package com.dobbinsoft.fw.oss;


import com.dobbinsoft.fw.TestsApplication;
import com.dobbinsoft.fw.support.properties.FwObjectStorageProperties;
import com.dobbinsoft.fw.support.storage.StorageClient;
import com.dobbinsoft.fw.support.storage.StoragePrivateResult;
import com.dobbinsoft.fw.support.storage.StorageRequest;
import com.dobbinsoft.fw.support.storage.StorageResult;
import com.dobbinsoft.fw.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StreamUtils;

import java.io.*;

@Slf4j
@SpringBootTest(classes = TestsApplication.class, properties = {
        // 数据库连接
        "spring.application.name=support",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.password=xxxxx",
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.url=jdbc:mysql://192.168.123.180:31234/demo?autoReconnect=true&useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true",
        "spring.datasource.username=demo",
        // Redis连接
        "spring.redis.database=0",
        "spring.redis.host=192.168.123.180:31235",
        "spring.lock-redis.database=2",
        "spring.lock-redis.host=192.168.123.180:31235",
        "spring.redis.master-name=mymaster",
        "spring.redis.mode=single",
        "spring.user-redis.database=1",
        "spring.user-redis.host=192.168.123.180:31235",
        // OSS 功能测试配置
        "com.dobbinsoft.oss.enable=aliyun",
        "com.dobbinsoft.oss.ali-access-key-id=LxxxxxxxxxxxxxxQ",
        "com.dobbinsoft.oss.ali-access-key-secret=Xxxxxxxxxxxxv",
        "com.dobbinsoft.oss.ali-endpoint=oss-cn-shanghai.aliyuncs.com",
        "com.dobbinsoft.oss.ali-bucket=demo-qa",
        "com.dobbinsoft.oss.ali-base-url=https://demo-qa.oss-cn-shanghai.aliyuncs.com",
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AliOssTests {

    @Autowired
    private StorageClient storageClient;

    @Autowired
    private FwObjectStorageProperties fwObjectStorageProperties;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private static final String TEST_FILE = "/Users/w.wei/Documents/testFile.jpg";

    @Test
    @Order(1)
    public void testUpload() throws FileNotFoundException {
        StorageRequest storageRequest = new StorageRequest();
        File file = new File(TEST_FILE);
        FileInputStream fis = new FileInputStream(file);
        storageRequest.setIs(fis);
        storageRequest.setPath("test");
        storageRequest.setFilename("testFile." + StringUtils.getFileExtension(TEST_FILE));
        storageRequest.setSize(file.length());
        storageRequest.setContentType("image/jpg");
        StorageResult result = storageClient.save(storageRequest);
        log.info("[测试上传公开] 成功 URL:{}", result.getUrl());
    }

    @Test
    @Order(2)
    public void testGetPublic() throws IOException {
        Response response = okHttpClient.newCall(new Request.Builder()
                .url(fwObjectStorageProperties.getAliBaseUrl() + "/test/testFile.jpg")
                .build()).execute();
        if (response.code() != 200) {
            throw new RuntimeException("测试失败");
        }
        ResponseBody body = response.body();
        log.info("[测试下载公开] 成功 文件长度:{}, 文件类型:{}", body.contentLength(), body.contentType().toString());
    }

    @Test
    @Order(3)
    public void testDeletePublic() {
        storageClient.delete(fwObjectStorageProperties.getAliBaseUrl() + "/test/testFile.jpg");
        log.info("[测试删除公开] 成功");
    }

    @Test
    @Order(4)
    public void testUploadPrivate() throws IOException {
        StorageRequest storageRequest = new StorageRequest();
        File file = new File(TEST_FILE);
        FileInputStream fis = new FileInputStream(file);
        storageRequest.setIs(fis);
        storageRequest.setPath("test");
        storageRequest.setFilename("testFilePrivate." + StringUtils.getFileExtension(TEST_FILE));
        storageRequest.setSize(file.length());
        storageRequest.setContentType("image/jpg");
        StoragePrivateResult storagePrivateResult = storageClient.savePrivate(storageRequest);
        Response response = okHttpClient.newCall(new Request.Builder()
                .url(fwObjectStorageProperties.getAliBaseUrl() + "/test/testFile.jpg")
                .build()).execute();
        if (response.code() == 200) {
            throw new RuntimeException("测试失败：私有文件不能公开访问");
        }
        // 读取IO
        response.body();
        log.info("[测试上传私有] 成功 私有URL={}", storagePrivateResult.getUrl());
    }

    @Test
    @Order(5)
    public void testGetPrivate() throws IOException {
        String privateUrl = storageClient.getPrivateUrl("test/testFilePrivate.jpg", 60 * 5);
        Response response = okHttpClient.newCall(new Request.Builder()
                .url(privateUrl)
                .build()).execute();
        if (response.code() != 200) {
            throw new RuntimeException("测试失败");
        }
        log.info("[测试下载私有] 成功 URL={}", privateUrl);
    }

    @Test
    @Order(6)
    public void testDownloadPrivate() throws IOException {
        try (InputStream download = storageClient.download("test/testFilePrivate.jpg")) {
            byte[] bytes = StreamUtils.copyToByteArray(download);
            log.info("[测试下载私有] 成功 bytes.length:{}", bytes.length);
        }
    }

    @Test
    @Order(7)
    public void testDeletePrivate() {
        storageClient.deletePrivate("test/testFilePrivate.jpg");
        log.info("[测试删除私有] 成功");
    }

}
