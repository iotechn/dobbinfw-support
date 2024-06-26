package com.dobbinsoft.fw.support.utils;

import java.util.UUID;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static String toGetterMethod(String fieldName) {
        // 首先检查输入字符串是否为空或null
        if (fieldName == null || fieldName.isEmpty()) {
            return fieldName; // 或抛出异常
        }
        // 转换：首字母大写 + 前缀"get"
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }


    public static String convertGetterToFieldName(String getterName) {
        if (getterName == null) {
            return null;
        }

        // 移除get或is前缀
        String fieldPart;
        if (getterName.startsWith("get")) {
            fieldPart = getterName.substring(3);
        } else if (getterName.startsWith("is")) {
            fieldPart = getterName.substring(2);
        } else {
            // 不符合getter方法命名的字符串，返回原字符串或null或抛出异常
            return getterName;  // 或者选择抛出一个异常
        }

        // 将首字母转换为小写
        return fieldPart.substring(0, 1).toLowerCase() + fieldPart.substring(1);
    }


    // 驼峰命名转下划线命名
    public static String toUnderlineCase(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c)) {
                builder.append('_').append(Character.toLowerCase(c));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    // 下划线命名转驼峰命名
    public static String toCamelCase(String underscore) {
        if (underscore == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : underscore.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    builder.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    public static String lowerFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return str;
        }
        return Character.toLowerCase(firstChar) + str.substring(1);
    }

    public static String upperFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return str;
        }
        return Character.toUpperCase(firstChar) + str.substring(1);
    }


    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }

    // 生成uuid
    // Generate random numbers
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }



}
