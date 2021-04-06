package com.dobbinsoft.fw.support.config.db.injector;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.lang.reflect.Field;
import java.util.Date;

/**
 * ClassName: FwTableInitInjector
 * Description: TODO
 *
 * @author: e-weichaozheng
 * @date: 2021-03-31
 */
public class TableInit extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = this.getCreateSql(modelClass, tableInfo.getTableName());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, "tableInit", sqlSource);
    }

    public String getCreateSql(Class entityClass, String tableName) {
        // 开始构建SQL
        Field[] declaredFields = entityClass.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `");
        sb.append(tableName);
        sb.append("` (\n");
        sb.append("`id` bigint(20) NOT NULL AUTO_INCREMENT,\n");
        for (Field field : declaredFields) {
            Class<?> fieldType = field.getType();
            if (fieldType == Long.class) {
                sb.append("`");
                sb.append(StrUtil.toUnderlineCase(field.getName()));
                sb.append("` bigint(20) NOT NULL,\n");
            } else if (fieldType == String.class) {
                sb.append("`");
                sb.append(StrUtil.toUnderlineCase(field.getName()));
                sb.append("` varchar(255) NOT NULL,\n");
            } else if (fieldType == Integer.class) {
                sb.append("`");
                sb.append(StrUtil.toUnderlineCase(field.getName()));
                sb.append("` int(11) NOT NULL,\n");
            } else if (fieldType == Date.class) {
                sb.append("`");
                sb.append(StrUtil.toUnderlineCase(field.getName()));
                sb.append("` datetime NOT NULL,\n");
            }
        }
        sb.append("`gmt_update` datetime NOT NULL,\n" +
                "`gmt_create` datetime NOT NULL,");
        sb.append("PRIMARY KEY (`id`)\n");
        sb.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;");
        return sb.toString();
    }
}
