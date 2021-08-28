package com.dobbinsoft.fw.support.mapper.test;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dobbinsoft.fw.support.annotation.cache.CacheAssemble;
import com.dobbinsoft.fw.support.annotation.cache.CacheKeyPut;
import com.dobbinsoft.fw.support.domain.SuperDO;
import com.dobbinsoft.fw.support.mapper.IMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

public interface TestMapper extends IMapper<SuperDO> {

    List<SuperDO> selectList(@Param(Constants.WRAPPER) Wrapper<SuperDO> queryWrapper);

    @CacheKeyPut(key = "'XXX' + #entity.id", value = "#entity")
    int insert(SuperDO entity);

    @CacheAssemble(key = "'XXX' + #id")
    SuperDO selectById(Serializable id);

}
