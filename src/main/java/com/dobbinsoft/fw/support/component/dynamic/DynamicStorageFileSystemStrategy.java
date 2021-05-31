package com.dobbinsoft.fw.support.component.dynamic;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * ClassName: DynamicStorageFileSystemStrategy
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-05-20
 */
public class DynamicStorageFileSystemStrategy implements DynamicStorageStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DynamicStorageFileSystemStrategy.class);

    @Override
    public void write(String key, String value) {
        String filepath = System.getProperty("user.dir");
        String keyFile = filepath + "/" + key.replace(":", "/") + ".txt";
        try {
            File file = new File(keyFile);
            file.deleteOnExit();
            file.createNewFile();
            FileUtil.writeString(value, file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("[配置写入持久层] IO异常 keyPath=" + keyFile, e);
        }
    }

    @Override
    public String read(String key) {
        String filepath = System.getProperty("user.dir");
        String keyFile = filepath + "/" + key.replace(":", "/") + ".txt";
        try {
            return FileUtil.readString(new File(keyFile), StandardCharsets.UTF_8);
        } catch (IORuntimeException e) {
            logger.error("[配置读取持久层] IO异常 keyPath=" + keyFile);
            return null;
        }
    }

    @Override
    public boolean del(String key) {
        String filepath = System.getProperty("user.dir");
        String keyFile = filepath + "/" + key.replace(":", "/") + ".txt";
        File file = new File(keyFile);
        if (file.exists()) {
            file.deleteOnExit();
            return true;
        }
        return false;
    }
}
