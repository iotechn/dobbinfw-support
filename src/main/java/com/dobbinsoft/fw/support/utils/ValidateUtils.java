package com.dobbinsoft.fw.support.utils;

import com.dobbinsoft.fw.core.Const;
import com.dobbinsoft.fw.core.annotation.param.NotNull;
import com.dobbinsoft.fw.core.annotation.param.Range;
import com.dobbinsoft.fw.core.annotation.param.TextFormat;
import com.dobbinsoft.fw.core.exception.CoreExceptionDefinition;
import com.dobbinsoft.fw.core.exception.ServiceException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collection;

public class ValidateUtils {

    /**
     * 校验粗粒度接口参数，递归校验
     *
     * @param object
     * @throws ServiceException
     */
    public static void checkParam(Object object) throws ServiceException {
        try {
            Class<?> objectClazz = object.getClass();
            Field[] declaredFields = objectClazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                // 1. 非空
                NotNull notNull = field.getAnnotation(NotNull.class);
                if (notNull != null) {
                    if (field.getType() == String.class && StringUtils.isBlank((String) field.get(object))) {
                        // String 传空格也算空
                        throwParamCheckServiceException(notNull);
                    } else if (ObjectUtils.isEmpty(field.get(object))) {
                        throwParamCheckServiceException(notNull);
                    }
                }
                // 2. 范围
                Class<?> fieldClazz = field.getType();
                if (Number.class.isAssignableFrom(fieldClazz)) {
                    Range range = field.getAnnotation(Range.class);
                    Object numberObject = field.get(object);
                    if (numberObject != null) {
                        if (fieldClazz == Integer.class) {
                            if (range != null) {
                                int number = Integer.parseInt(numberObject.toString());
                                if (number > range.max() || number < range.min()) {
                                    throwParamCheckServiceException(range);
                                }
                            }
                        } else if (fieldClazz == Long.class) {
                            if (range != null) {
                                long number = Long.parseLong(numberObject.toString());
                                if (number > range.max() || number < range.min()) {
                                    throwParamCheckServiceException(range);
                                }
                            }
                        } else if (fieldClazz == Float.class) {
                            if (range != null) {
                                float number = Float.parseFloat(numberObject.toString());
                                if (number > range.max() || number < range.min()) {
                                    throwParamCheckServiceException(range);
                                }
                            }
                        } else if (fieldClazz == Double.class) {
                            if (range != null) {
                                double number = Double.parseDouble(numberObject.toString());
                                if (number > range.max() || number < range.min()) {
                                    throwParamCheckServiceException(range);
                                }
                            }
                        }
                    }
                }
                // 3. 递归其他非基本类型
                if (!Modifier.isStatic(field.getModifiers()) && !Const.IGNORE_PARAM_LIST.contains(fieldClazz)) {
                    if (Collection.class.isAssignableFrom(fieldClazz)) {
                        // 3.1. Collection
                        Collection<?> collection = (Collection<?>) field.get(object);
                        if (collection != null) {
                            for (Object obj : collection) {
                                if (!Const.IGNORE_PARAM_LIST.contains(obj.getClass())) {
                                    checkParam(obj);
                                }
                            }
                        }
                    } else if (!Const.IGNORE_DEEP_PARAM_LIST.contains(fieldClazz) && !fieldClazz.getTypeName().startsWith("java")) {
                        // 3.2. 其他对象
                        Object obj = field.get(object);
                        if (obj != null) {
                            checkParam(obj);
                        }
                    }
                }
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    /**
     * 校验细粒度接口参数
     *
     * @param type
     * @param methodParam
     * @param target
     * @throws ServiceException
     */
    public static void checkParam(Class<?> type, Parameter methodParam, String target) throws ServiceException {
        NotNull notNull = type.getAnnotation(NotNull.class);
        if (notNull != null && StringUtils.isBlank(target)) {
            throwParamCheckServiceException(notNull);
        }
        if (StringUtils.isBlank(target)) {
            // 无需校验空参数
            return;
        }
        if (type == String.class) {
            TextFormat textFormat = methodParam.getAnnotation(TextFormat.class);
            if (textFormat != null) {
                String regex = textFormat.regex();
                if (StringUtils.isNotEmpty(regex)) {
                    //如果正则生效，则直接使用正则校验
                    if (!target.matches(regex)) {
                        throwParamCheckServiceException(textFormat);
                    }
                } else {
                    boolean notChinese = textFormat.notChinese();
                    if (notChinese) {
                        if (target.matches("[\\u4e00-\\u9fa5]+")) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }

                    String[] contains = textFormat.contains();
                    for (String contain : contains) {
                        if (!target.contains(contain)) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }

                    String[] notContains = textFormat.notContains();
                    for (String notContain : notContains) {
                        if (target.contains(notContain)) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }

                    String startWith = textFormat.startWith();
                    if (StringUtils.isNotEmpty(startWith)) {
                        if (!target.startsWith(startWith)) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }

                    String endsWith = textFormat.endsWith();
                    if (StringUtils.isNotEmpty(target)) {
                        if (!target.endsWith(endsWith)) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }
                    int targetLength = target.length();
                    int length = textFormat.length();
                    if (length != -1) {
                        if (targetLength != length) {
                            throwParamCheckServiceException(textFormat);
                        }
                    }

                    if (targetLength < textFormat.lengthMin()) {
                        throwParamCheckServiceException(textFormat);
                    }

                    if (targetLength > textFormat.lengthMax()) {
                        throwParamCheckServiceException(textFormat);
                    }
                }
            }
        } else if (type == Integer.class) {
            Range range = methodParam.getAnnotation(Range.class);
            int integer = Integer.parseInt(target);
            if (range != null) {
                if (integer > range.max() || integer < range.min()) {
                    throwParamCheckServiceException(range);
                }
            }
        } else if (type == Long.class) {
            Range range = methodParam.getAnnotation(Range.class);
            if (range != null) {
                long integer = Long.parseLong(target);
                if (integer > range.max() || integer < range.min()) {
                    throwParamCheckServiceException(range);
                }
            }
        } else if (type == Float.class) {
            Range range = methodParam.getAnnotation(Range.class);
            if (range != null) {
                float number = Float.parseFloat(target);
                if (number > range.max() || number < range.min()) {
                    throwParamCheckServiceException(range);
                }
            }
        } else if (type == Double.class) {
            Range range = methodParam.getAnnotation(Range.class);
            if (range != null) {
                double number = Double.parseDouble(target);
                if (number > range.max() || number < range.min()) {
                    throwParamCheckServiceException(range);
                }
            }
        }
    }

    public static void throwParamCheckServiceException(Annotation annotation) throws ServiceException {
        try {
            Method method = annotation.getClass().getMethod("message");
            Object res = method.invoke(annotation);
            if (!org.springframework.util.ObjectUtils.isEmpty(res)) {
                throw new ServiceException((String) res, CoreExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED.getCode());
            } else {
                throw new ServiceException(CoreExceptionDefinition.LAUNCHER_PARAM_CHECK_FAILED);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

}
