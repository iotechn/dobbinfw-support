package com.dobbinsoft.fw.support.mapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dobbinsoft.fw.core.util.ReflectUtil;
import com.dobbinsoft.fw.support.annotation.ForeignKey;
import com.dobbinsoft.fw.support.annotation.LeafTable;
import com.dobbinsoft.fw.support.annotation.enums.ListLeafType;
import com.dobbinsoft.fw.support.context.QueryContext;
import com.dobbinsoft.fw.support.domain.SuperDO;
import com.dobbinsoft.fw.support.model.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

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
     *
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
        return insertDto(dto, getFieldList(dto.getClass()));
    }

    /**
     * 插入DTO，并插入子表
     *
     * @param dto
     * @param fieldList
     * @return
     */
    default Integer insertDto(Object dto, String... fieldList) {
        int effect = 0;
        Class<T> entityClass = this.getEntityClass();
        if (entityClass != null) {
            try {
                T instance = (T) entityClass.newInstance();
                BeanUtils.copyProperties(dto, instance);
                effect = this.insert(instance);
                Method getIdMethod = cn.hutool.core.util.ReflectUtil.getMethod(instance.getClass(), "getId");
                Object primaryKey = getIdMethod.invoke(instance);
                try {
                    Method method = dto.getClass().getMethod("setId", Long.class);
                    method.invoke(dto, primaryKey);
                } catch (NoSuchMethodException e) {

                }
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
                            Field field = dtoClass.getDeclaredField(filedStr);
                            boolean batch = false;
                            LeafTable leafTable = field.getAnnotation(LeafTable.class);
                            if (leafTable != null) {
                                batch = leafTable.batch();
                            }
                            if (batch) {
                                // 若是批量插入
                                mapper.insertBatchSomeColumn(subList);
                            } else {
                                // 单个插入
                                for (Object subItem : subList) {
                                    mapper.insert(subItem);
                                }
                            }
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
                e.printStackTrace();
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
     * 从查询上下文中获取条件，并且分页查询
     *
     * @param page 分页查询条件（可以为 RowBounds.DEFAULT）
     */
    default <E extends IPage<T>> E selectPage(E page) {
        return this.selectPage(page, (Wrapper<T>) QueryContext.get());
    }

    /**
     * 从查询上下文中获取条件
     */
    default List<T> selectList() {
        return this.selectList(QueryContext.get());
    }

    /**
     * 分页查取dto
     *
     * @param page
     * @param queryWrapper
     * @param clazz
     * @param <DTO>
     * @return
     */
    default <DTO> Page<DTO> selectPageDto(Page<T> page, Wrapper<T> queryWrapper, Class<DTO> clazz) {
        Page<T> domainPage = this.selectPage(page, queryWrapper);
        Class<T> entityClass = this.getEntityClass();
        if (entityClass != null) {
            String[] fieldListInList = this.getFieldListInList(clazz);
            Map<String, Map<Object, List>> paramsMap = new HashMap<>();
            if (fieldListInList.length > 0) {
                Method getId = cn.hutool.core.util.ReflectUtil.getMethod(entityClass, "getId");
                Set<Object> ids = domainPage.getRecords().stream().map(item -> {
                    try {
                        Object id = getId.invoke(item);
                        return id;
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException("该对象未设置主键");
                    }
                }).collect(Collectors.toSet());
                if (!CollectionUtils.isEmpty(ids)) {
                    // 若不为空
                    try {
                        for (String fieldStr : fieldListInList) {
                            Field field = clazz.getDeclaredField(fieldStr);
                            if (Collection.class.isAssignableFrom(field.getType())) {
                                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                                // 列表内元素类
                                Class itemClazz = (Class) parameterizedType.getActualTypeArguments()[0];
                                // 获取外键
                                Field[] itemFields = itemClazz.getDeclaredFields();
                                inner:
                                for (Field itemField : itemFields) {
                                    ForeignKey foreignKey = itemField.getAnnotation(ForeignKey.class);
                                    if (foreignKey != null) {
                                        String name = StrUtil.toUnderlineCase(itemField.getName());
                                        IMapper itemMapper = MapperManager.map.get(itemClazz);
                                        List childrenList = itemMapper.selectList((Wrapper) new QueryWrapper().in(name, ids));
                                        // 以外键属性分组
                                        Map<Object, List> map = (Map<Object, List>) childrenList.stream().collect(Collectors.groupingBy(item -> {
                                            Object foreignKeyId = null;
                                            try {
                                                Method getForeignKeyMethod = itemClazz.getMethod(ReflectUtil.getMethodName(itemField.getName(), "get"));
                                                foreignKeyId = getForeignKeyMethod.invoke(item);
                                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                            return foreignKeyId;
                                        }));
                                        paramsMap.put(fieldStr, map);
                                        break inner;
                                    }
                                }
                            }
                        }
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                    // 循环将这个trans进去

                }
            }
            return domainPage.trans(item -> {
                DTO dto = null;
                try {
                    dto = clazz.newInstance();
                    BeanUtils.copyProperties(item, dto);
                    Set<String> keys = paramsMap.keySet();
                    // key dto 中 Collection属性中的 field 字符串名字
                    for (String key : keys) {
                        Map<Object, List> paramMap = paramsMap.get(key);
                        if (!CollectionUtils.isEmpty(paramMap)) {
                            // 取出Item的主键
                            Method getIdMethod = item.getClass().getMethod("getId");
                            Object mainPk = getIdMethod.invoke(item);
                            List list = paramMap.get(mainPk);
                            Method setListMethod = clazz.getMethod(ReflectUtil.getMethodName(key, "set"), List.class);
                            setListMethod.invoke(dto, list);
                        }
                    }
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return dto;
            });
        }
        return null;
    }


    /********************** 以下可以理解为私有方法 *********************/

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

    /**
     * 通过注解获取，需要被 selectPageDto 和 selectListDto 查询到的叶子子表
     *
     * @param clazz
     * @param <DTO>
     * @return
     */
    default <DTO> String[] getFieldListInList(Class<DTO> clazz) {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<String> list = new LinkedList<>();
        for (Field field : declaredFields) {
            LeafTable leafTable = field.getAnnotation(LeafTable.class);
            if (leafTable != null) {
                if (leafTable.containsList() == ListLeafType.WITH) {
                    list.add(field.getName());
                }
            }
        }
        return list.toArray(new String[0]);
    }


    default Class<T> getEntityClass() {
        Class<? extends IMapper> clazz = this.getClass();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
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
                                return entityClass;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

}
