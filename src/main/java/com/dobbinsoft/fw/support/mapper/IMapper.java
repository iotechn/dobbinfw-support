package com.dobbinsoft.fw.support.mapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dobbinsoft.fw.core.util.ReflectUtil;
import com.dobbinsoft.fw.support.annotation.ForeignKey;
import com.dobbinsoft.fw.support.annotation.LeafTable;
import com.dobbinsoft.fw.support.domain.SuperDO;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Description:
 * User: rize
 * Date: 2020/8/1
 * Time: 10:54
 */
public interface IMapper<T> extends BaseMapper<T> {

    /**
     * 高效率批量插入 仅支持 MYSQL
     *
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<T> entityList);

    /**
     * 初始化表
     * @return
     */
    Integer tableInit();

    /**
     * 查询Dto，并获取子表数据
     *
     * @param id
     * @param clazz
     * @param <DTO>
     * @return
     */
    default <DTO> DTO selectByIdDto(Long id, Class<DTO> clazz) {
        return selectByIdDto(id, clazz, getFieldList(clazz));
    }

    /**
     * 查询Dto，并获取子表数据
     *
     * @param id
     * @param clazz
     * @param fieldList
     * @param <DTO>
     * @return
     */
    default <DTO> DTO selectByIdDto(Long id, Class<DTO> clazz, String... fieldList) {
        T t = this.selectById(id);
        if (t == null) {
            return null;
        }
        DTO dto = null;
        try {
            dto = clazz.newInstance();
            BeanUtils.copyProperties(t, dto);
            for (String fieldStr : fieldList) {
                Field field = clazz.getDeclaredField(fieldStr);
                if (Collection.class.isAssignableFrom(field.getType())) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    Class itemClazz = (Class) parameterizedType.getActualTypeArguments()[0];
                    Field[] itemFields = itemClazz.getDeclaredFields();
                    String name = null;
                    for (Field itemField : itemFields) {
                        ForeignKey annotation = itemField.getAnnotation(ForeignKey.class);
                        if (annotation != null) {
                            name = StrUtil.toUnderlineCase(itemField.getName());
                        }
                    }
                    if (name != null) {
                        IMapper mapper = MapperManager.map.get(itemClazz);
                        if (mapper == null) {
                            throw new RuntimeException("请确认 " + itemClazz.toGenericString() + " 所对应Mapper存在");
                        }
                        List list = mapper.selectList(new QueryWrapper<Object>().eq(name, id));
                        Method method = clazz.getMethod(ReflectUtil.getMethodName(fieldStr, "set"), field.getType());
                        method.invoke(dto, list);
                    }
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            // 反射异常
            e.printStackTrace();
        }
        return dto;
    }

    /**
     * 插入DTO，并插入子表
     *
     * @param dto
     * @return
     */
    default Integer insertDto(Object dto) {
        Class<? extends IMapper> clazz = this.getClass();
        return insertDto(dto, getFieldList(clazz));
    }

    /**
     * 插入DTO，并插入子表
     *
     * @param dto
     * @param fieldList
     * @return
     */
    default Integer insertDto(Object dto, String... fieldList) {
        Class<? extends IMapper> clazz = this.getClass();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        int effect = 0;
        for (Type type : genericInterfaces) {
            if (type instanceof Class) {
                Class mapperClazz = (Class) type;
                Type[] iMapperTypes = mapperClazz.getGenericInterfaces();
                if (iMapperTypes.length > 0) {
                    for (Type t : iMapperTypes) {
                        if (t instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) t;
                            if (IMapper.class.isAssignableFrom((Class) pt.getRawType())) {
                                // 若是IMapper的子类
                                Class entityClass = (Class) pt.getActualTypeArguments()[0];
                                try {
                                    T instance = (T) entityClass.newInstance();
                                    BeanUtils.copyProperties(dto, instance);
                                    effect = this.insert(instance);
                                    Method getIdMethod = cn.hutool.core.util.ReflectUtil.getMethod(instance.getClass(), "getId");
                                    Object primaryKey = getIdMethod.invoke(instance);
                                    // 子表
                                    Class<?> dtoClass = dto.getClass();
                                    for (String filedStr : fieldList) {
                                        Method get = dtoClass.getMethod(ReflectUtil.getMethodName(filedStr, "get"));
                                        if (get.invoke(dto) instanceof Collection) {
                                            Collection subList = (Collection) get.invoke(dto);
                                            if (!CollectionUtils.isEmpty(subList)) {
                                                // 找到子表的Mapper
                                                Class subClazz = null;
                                                Method fKeyMethod = null;
                                                for (Object subObj : subList) {
                                                    if (subClazz == null) {
                                                        subClazz = subObj.getClass();
                                                        // 找出子表外键，并将primaryKey设置进去
                                                        Field[] subClazzDeclaredFields = subClazz.getDeclaredFields();
                                                        for (Field subField : subClazzDeclaredFields) {
                                                            ForeignKey foreignKey = subField.getAnnotation(ForeignKey.class);
                                                            if (foreignKey != null) {
                                                                fKeyMethod = cn.hutool.core.util.ReflectUtil.getMethod(subClazz,
                                                                        ReflectUtil.getMethodName(subField.getName(), "set"), primaryKey.getClass());
                                                            }
                                                        }
                                                    }
                                                    if (fKeyMethod == null) {
                                                        throw new RuntimeException("子表未设置外键:" + subClazz.getName());
                                                    }
                                                    // 设置外键
                                                    fKeyMethod.invoke(subObj, primaryKey);
                                                }
                                                IMapper mapper = MapperManager.map.get(subClazz);
                                                if (mapper == null) {
                                                    throw new RuntimeException("请确认 " + subClazz.toGenericString() + " 所对应Mapper存在");
                                                }
                                                mapper.insertBatchSomeColumn(subList);
                                            }
                                        }
                                    }
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
        return effect;
    }

    default Integer deleteByIdDto(Serializable id, Class<?> clazz) {
        return deleteByIdDto(id, clazz, getFieldList(clazz));
    }
    /**
     * 删除DTO，并删除其子表数据
     *
     * @param id        主键
     * @param clazz
     * @param fieldList
     * @return
     */
    default Integer deleteByIdDto(Serializable id, Class<?> clazz, String... fieldList) {
        // 1.删除主表
        int effect = this.deleteById(id);
        // 2.找到从表
        if (effect > 0) {
            for (String fieldStr : fieldList) {
                try {
                    Field field = clazz.getDeclaredField(fieldStr);
                    Type genericType = field.getGenericType();
                    if (genericType instanceof ParameterizedType) {
                        ParameterizedType pt = (ParameterizedType) genericType;
                        Type actualTypeArgument = pt.getActualTypeArguments()[0];
                        if (actualTypeArgument instanceof Class) {
                            if (SuperDO.class.isAssignableFrom((Class<?>) actualTypeArgument)) {
                                // 若是DO派生类
                                Class subActualType = (Class) actualTypeArgument;
                                // 找到DO的外键
                                Field[] subActualTypeDeclaredFields = subActualType.getDeclaredFields();
                                for (Field subField : subActualTypeDeclaredFields) {
                                    if (subField.getAnnotation(ForeignKey.class) != null) {
                                        IMapper mapper = MapperManager.map.get(subActualType);
                                        if (mapper == null) {
                                            throw new RuntimeException("请确认 " + subActualType.toGenericString() + " 所对应Mapper存在");
                                        }
                                        mapper.delete(new QueryWrapper<Object>().eq(StrUtil.toUnderlineCase(subField.getName()), id));
                                    }
                                }
                            }

                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
        return effect;
    }


    /**
     * 通过注解获取属性，抽取方法
     *
     * @param clazz
     * @param <DTO>
     * @return
     */
    default <DTO> String[] getFieldList(Class<DTO> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<String> list = new LinkedList<>();
        for (Field field : declaredFields) {
            if (field.getAnnotation(LeafTable.class) != null) {
                list.add(field.getName());
            }
        }
        return list.toArray(new String[0]);
    }

}
