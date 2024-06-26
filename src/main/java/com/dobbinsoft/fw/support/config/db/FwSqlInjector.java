package com.dobbinsoft.fw.support.config.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.dobbinsoft.fw.support.config.db.injector.InsertOnDuplicateUpdate;
import com.dobbinsoft.fw.support.config.db.injector.TableInit;

import java.util.List;

/**
 * ClassName: FwSqlInjector
 * Description: 配置SQL注入器
 */
public class FwSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        // 相同字段批量插入
        methodList.add(new InsertBatchSomeColumn(i -> i.getFieldFill() != FieldFill.UPDATE));
        // 若出现唯一键冲突，则该行改为更新，否则插入
        methodList.add(new InsertOnDuplicateUpdate());
//        methodList.add(new InsertOnDuplicateUpdateBatch(i -> i.getFieldFill() != FieldFill.UPDATE));
        methodList.add(new TableInit());
        return methodList;
    }
}
