package com.dobbinsoft.fw.support.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dobbinsoft.fw.support.utils.BeanUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: 所有Mapper的基类
 * @param <T> 实体类
 */
public interface IMapper<T> extends BaseMapper<T> {

    /**
     * 高效率批量插入 仅支持 MYSQL
     *
     * @param entityList 实体列表
     * @return 成功插入数量
     */
    Integer insertBatchSomeColumn(Collection<T> entityList);

    /**
     * 初始化表
     */
    void tableInit();


    /**
     * 插入一条数据，若此数据出现唯一键冲突，则更新该行数据
     * @param entity
     * @return
     */
    int insertOnDuplicateUpdate(T entity);

    default List<T> selectList() {
        return this.selectList(null);
    }

    default <DTO> DTO selectByIdDto(Serializable id, Class<DTO> clazz) {
        T t = this.selectById(id);
        if (t == null) {
            return null;
        }
        try {
            DTO dto = clazz.getConstructor().newInstance();
            BeanUtils.copyProperties(t, dto);
            return dto;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("请为DTO添加public的无参构造器");
        }
    }


}
