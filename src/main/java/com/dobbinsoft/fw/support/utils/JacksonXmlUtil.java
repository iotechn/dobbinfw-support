package com.dobbinsoft.fw.support.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class JacksonXmlUtil {

    public static final XmlMapper objectMapper;
    public static final XmlMapper objectMapperWithoutNull;

    static {
        objectMapper = buildInstance();
        objectMapperWithoutNull = buildInstance();
        //在序列化时忽略值为 null 的属性
        objectMapperWithoutNull.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * xml字符串反序列化为Java对象
     *
     * @param xmlString JSON字符串
     * @param classType  对象类型
     * @param <T> 类型
     * @return T
     */
    public static <T> T parseObject(String xmlString, Class<T> classType) {
        if (StringUtils.isEmpty(xmlString)) {
            return null;
        }
        try {
            return objectMapper.readValue(xmlString, classType);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * xml 反序列化为
     * @param xmlString
     * @param tTypeReference
     * @param <T> 类型
     * @return T
     */
    public static <T> T parseObject(String xmlString, TypeReference<T> tTypeReference) {
        if (StringUtils.isEmpty(xmlString)) {
            return null;
        }
        try {
            return objectMapper.readValue(xmlString, tTypeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 普通XML序列化
     *
     * @param obj 目标对象
     * @return String
     */
    public static String toXmlString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 普通XML序列化
     *
     * @param obj 目标对象
     * @return String
     */
    public static String toXmlStringWithoutNull(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapperWithoutNull.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }


    private static XmlMapper buildInstance() {
        XmlMapper instance = new XmlMapper();
        return (XmlMapper) JacksonUtil.setMapperConfig(instance);
    }

}
