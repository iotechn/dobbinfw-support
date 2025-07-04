package com.dobbinsoft.fw.support.utils;

import java.io.*;
import java.util.Base64;

public class SerializationUtils {

    private SerializationUtils() {
        // 私有构造函数防止实例化
    }

    /**
     * 将对象序列化为Base64字符串
     * @param obj 可序列化对象（必须实现Serializable接口）
     * @return Base64编码的字符串
     */
    public static String serializeToString(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

    /**
     * 将Base64字符串反序列化为对象
     * @param s Base64编码的字符串
     * @return 反序列化后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeFromString(String s) {
        byte[] data = Base64.getDecoder().decode(s);
        
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ioException) {
            throw new RuntimeException(ioException);
        }
    }
}