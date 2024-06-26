package com.dobbinsoft.fw.support.config.db.injector;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;

public class InsertOnDuplicateUpdate extends AbstractMethod {

    public static final String METHOD = "insertOnDuplicateUpdate";

    public InsertOnDuplicateUpdate() {
        super(METHOD);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(null),
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        String valuesScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(null),
                LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);

        List<TableFieldInfo> fieldList = tableInfo.getFieldList();
        StringBuilder sb = new StringBuilder();
        sb.append("<script>\n");

        sb.append(String.format("INSERT INTO %s %s VALUES %s ON DUPLICATE KEY UPDATE ", tableInfo.getTableName(), columnScript, valuesScript));
        for (int i = 0; i < fieldList.size(); i++) {
            TableFieldInfo tableFieldInfo = fieldList.get(i);
            String column = tableFieldInfo.getColumn();
            sb.append("<if test=\"");
            sb.append(tableFieldInfo.getProperty());
            sb.append(" != null");
            sb.append("\">");
            if (i != 0) {
                sb.append(",");
            }
            sb.append(column);
            sb.append(" = values(");
            sb.append(column);
            sb.append(")");
            sb.append("</if>");
        }
        sb.append("\n</script>");

        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /** 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (null != tableInfo.getKeySequence()) {
                    keyGenerator = TableInfoHelper.genKeyGenerator(METHOD, tableInfo, builderAssistant);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sb.toString(), modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, METHOD, sqlSource, keyGenerator, keyProperty, keyColumn);
    }

}
