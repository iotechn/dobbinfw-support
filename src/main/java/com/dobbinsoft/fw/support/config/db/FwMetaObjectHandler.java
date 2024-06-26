package com.dobbinsoft.fw.support.config.db;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * ClassName: FwMetaObjectHandler
 * Description: 自动完善基础字段
 *
 */
public class FwMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.fillStrategy(metaObject, "gmtUpdate", now);
        this.fillStrategy(metaObject, "gmtCreate", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.fillStrategy(metaObject, "gmtUpdate", LocalDateTime.now());
    }

}
