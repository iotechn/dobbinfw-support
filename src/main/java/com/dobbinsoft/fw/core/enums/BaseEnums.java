package com.dobbinsoft.fw.core.enums;

import com.dobbinsoft.fw.support.utils.StringUtils;

import java.io.Serializable;

/**
 * ClassName: BaseEnums
 * Description: 基本枚举
 *
 * @param <S> 枚举代码类型
 */
public interface BaseEnums<S extends Serializable> {

    public S getCode();

    public String getMsg();

    public static <S extends Serializable, T extends BaseEnums<S>> T getByCode(S s, Class<T> clazz) {
        BaseEnums<S>[] enumConstants = clazz.getEnumConstants();
        for (BaseEnums<S> baseEnums : enumConstants) {
            if (baseEnums.getCode().equals(s)) {
                return (T) baseEnums;
            }
        }
        return null;
    }

    public static <T extends Serializable> String getMsgByCode(T t, Class<? extends BaseEnums<T>> clazz) {
        BaseEnums<T> baseEnums = getByCode(t, clazz);
        if (baseEnums == null) {
            return null;
        }
        return baseEnums.getMsg();
    }

    public default String getMap() {
        Class<? extends BaseEnums> clazz = this.getClass();
        BaseEnums[] enumConstants = clazz.getEnumConstants();
        StringBuilder sb = new StringBuilder();
        sb.append("const ");
        sb.append(StringUtils.lowerFirst(clazz.getSimpleName()));
        sb.append("Map = {\n");

        for (int i = 0; i < enumConstants.length; i++) {
            sb.append("  ");
            sb.append(enumConstants[i].getCode());
            sb.append(": '");
            sb.append(enumConstants[i].getMsg());
            if (i == enumConstants.length -1) {
                sb.append("'\n}");
            } else {
                sb.append("',\n");
            }
        }
        return sb.toString();
    }


    public static String getKeyValue(Class<? extends BaseEnums> clazz) {
        BaseEnums[] enumConstants = clazz.getEnumConstants();
        StringBuilder sb = new StringBuilder();
        for (BaseEnums enumConstant : enumConstants) {
            sb.append(enumConstant.getCode());
            sb.append("-");
            sb.append(enumConstant.getMsg());
        }
        return sb.toString();
    }

}
