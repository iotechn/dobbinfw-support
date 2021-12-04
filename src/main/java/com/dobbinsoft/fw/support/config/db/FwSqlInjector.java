package com.dobbinsoft.fw.support.config.db;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.dobbinsoft.fw.support.config.db.injector.TableInit;

import java.util.List;

/**
 * ClassName: FwSqlInjector
 * Description: 配置SQL注入器
 *
 * @author: e-weichaozheng
 * @date: 2021-03-29
 */
public class FwSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new InsertBatchSomeColumn(i -> i.getFieldFill() != FieldFill.UPDATE));
        methodList.add(new TableInit());
        return methodList;
    }
}
