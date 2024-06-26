//package com.dobbinsoft.fw.support.config.db.syntax;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
//import com.dobbinsoft.fw.support.mapper.IMapper;
//import com.dobbinsoft.fw.support.model.Page;
//import org.apache.ibatis.annotations.Param;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.lang.reflect.*;
//import java.util.*;
//
///**
// * 语法检测, 建议只在DEV环境开启
// */
//public class SyntaxCheck implements InitializingBean {
//
//
//    // 注入所有的Mapper
//    @Autowired
//    private List<IMapper> iMappers;
//
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        // 1. 检查字段是否都正确
//        for (IMapper iMapper : iMappers) {
//            iMapper.selectList(new QueryWrapper<>().last(" LIMIT 0"));
//        }
//        // 2. 检查自定义SQL。前面注入一个Explain
//        for (IMapper iMapper : iMappers) {
//            // 获取子类的自定义SQL
//            Class<?>[] interfaces = iMapper.getClass().getInterfaces();
//            Optional<Class<?>> optional = Arrays.stream(interfaces).filter(IMapper.class::isAssignableFrom).findFirst();
//            if (optional.isEmpty()) {
//                continue;
//            }
//            Method[] declaredMethods = optional.get().getDeclaredMethods();
//            for (Method declaredMethod : declaredMethods) {
//                if (declaredMethod.isDefault() || Modifier.isStatic(declaredMethod.getModifiers())) {
//                    continue;
//                }
//                Parameter[] parameters = declaredMethod.getParameters();
//                List<Object> args = new ArrayList<>();
//                Map<String, Object> paramMap = new HashMap<>();
//                for (Parameter parameter : parameters) {
//                    Param annotation = parameter.getAnnotation(Param.class);
//                    Class<?> parameterType = parameter.getType();
//                    Object zeroValue = getZeroValue(parameterType);
//                    if (annotation != null) {
//                        paramMap.put(annotation.value(), zeroValue);
//                        if (!args.contains(paramMap)) {
//                            args.add(paramMap);
//                        }
//                    } else {
//                        args.add(zeroValue);
//                    }
//                }
//                SyntaxInterceptor.SWITCH.set(Boolean.TRUE);
//                try {
//                    declaredMethod.invoke(iMapper, args);
//                } finally {
//                    SyntaxInterceptor.SWITCH.set(Boolean.FALSE);
//                }
//            }
//        }
//    }
//
//
//    private static Object getZeroValue(Class<?> parameterType) {
//        if (parameterType == int.class || parameterType == Integer.class) {
//            return 0;
//        } else if (parameterType == long.class || parameterType == Long.class) {
//            return 0L;
//        } else if (parameterType == float.class || parameterType == Float.class) {
//            return 0.0f;
//        } else if (parameterType == double.class || parameterType == Double.class) {
//            return 0.0;
//        } else if (parameterType == short.class || parameterType == Short.class) {
//            return (short) 0;
//        } else if (parameterType == byte.class || parameterType == Byte.class) {
//            return (byte) 0;
//        } else if (parameterType == boolean.class || parameterType == Boolean.class) {
//            return false;
//        } else if (parameterType == char.class || parameterType == Character.class) {
//            return '\u0000';
//        } else if (parameterType == String.class) {
//            return "";
//        } else if (List.class.isAssignableFrom(parameterType)) {
//            return new ArrayList<>();
//        } else if (Set.class.isAssignableFrom(parameterType)) {
//            return new HashSet<>();
//        } else if (Map.class.isAssignableFrom(parameterType)) {
//            return new HashMap<>();
//        } else if (IPage.class.isAssignableFrom(parameterType)) {
//            return Page.div(1, 20, null);
//        } else {
//            try {
//                Constructor<?> constructor = parameterType.getConstructor();
//                Object o = constructor.newInstance();
//                setZeroValue(o);
//                return o;
//            } catch (Exception ignored) {
//            }
//        }
//        // 如果需要处理其他类型，可以在这里添加更多的类型判断
//        return null;
//    }
//
//
//    public static void setZeroValue(Object obj) throws IllegalAccessException {
//        if (obj == null) {
//            return;
//        }
//
//        Class<?> clazz = obj.getClass();
//        Field[] fields = clazz.getDeclaredFields();
//
//        for (Field field : fields) {
//            field.setAccessible(true);
//            Class<?> fieldType = field.getType();
//
//            if (fieldType == int.class) {
//                field.setInt(obj, 0);
//            } else if (fieldType == Integer.class) {
//                field.set(obj, 0);
//            } else if (fieldType == long.class) {
//                field.setLong(obj, 0L);
//            } else if (fieldType == Long.class) {
//                field.set(obj, 0L);
//            } else if (fieldType == float.class) {
//                field.setFloat(obj, 0.0f);
//            } else if (fieldType == Float.class) {
//                field.set(obj, 0.0f);
//            } else if (fieldType == double.class) {
//                field.setDouble(obj, 0.0);
//            } else if (fieldType == Double.class) {
//                field.set(obj, 0.0);
//            } else if (fieldType == short.class) {
//                field.setShort(obj, (short) 0);
//            } else if (fieldType == Short.class) {
//                field.set(obj, (short) 0);
//            } else if (fieldType == byte.class) {
//                field.setByte(obj, (byte) 0);
//            } else if (fieldType == Byte.class) {
//                field.set(obj, (byte) 0);
//            } else if (fieldType == boolean.class) {
//                field.setBoolean(obj, false);
//            } else if (fieldType == Boolean.class) {
//                field.set(obj, false);
//            } else if (fieldType == char.class) {
//                field.setChar(obj, '\u0000');
//            } else if (fieldType == Character.class) {
//                field.set(obj, '\u0000');
//            } else if (fieldType == String.class) {
//                field.set(obj, "");
//            } else if (List.class.isAssignableFrom(fieldType)) {
//                field.set(obj, new ArrayList<>());
//            } else if (Set.class.isAssignableFrom(fieldType)) {
//                field.set(obj, new HashSet<>());
//            } else if (Map.class.isAssignableFrom(fieldType)) {
//                field.set(obj, new HashMap<>());
//            } else {
//                try {
//                    Constructor<?> constructor = fieldType.getConstructor();
//                    Object o = constructor.newInstance();
//                    field.set(obj, o);
//                } catch (Exception e) {
//                    return;
//                }
//            }
//        }
//    }
//}
