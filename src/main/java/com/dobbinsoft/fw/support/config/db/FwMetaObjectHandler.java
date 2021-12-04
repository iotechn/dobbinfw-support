package com.dobbinsoft.fw.support.config.db;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * ClassName: FwMetaObjectHandler
 * Description: 自动完善基础字段
 *
 * @author: e-weichaozheng
 * @date: 2021-03-30
 */
public class FwMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        this.fillStrategy(metaObject, "gmtUpdate", now);
        this.fillStrategy(metaObject, "gmtCreate", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.fillStrategy(metaObject, "gmtUpdate", new Date());
    }

}
