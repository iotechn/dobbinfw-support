package com.dobbinsoft.fw.support.config.db.injector;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.dobbinsoft.fw.core.annotation.doc.ApiField;
import com.dobbinsoft.fw.core.annotation.param.NotNull;
import com.dobbinsoft.fw.core.annotation.param.TextFormat;
import com.dobbinsoft.fw.core.enums.BaseEnums;
import com.dobbinsoft.fw.core.enums.EmptyEnums;
import com.dobbinsoft.fw.support.utils.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * ClassName: TableInit
 * Description: 对应Table初始化
 *
 */
public class TableInit extends AbstractMethod {

    private static final String METHOD = "tableInit";

    public TableInit() {
        super(METHOD);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        String sql = this.getCreateSql(modelClass, tableInfo.getTableName());
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addDeleteMappedStatement(mapperClass, "tableInit", sqlSource);
    }

    private String getCreateSql(Class<?> entityClass, String tableName) {
        // 开始构建SQL
        Field[] declaredFields = entityClass.getDeclaredFields();
        StringBuilder sb = new StringBuilder();
        sb.append("SET NAMES utf8mb4;");
        sb.append("CREATE TABLE IF NOT EXISTS `");
        sb.append(tableName);
        sb.append("` (\n");
        sb.append("`id` bigint(20) NOT NULL AUTO_INCREMENT,\n");
        for (Field field : declaredFields) {
            Class<?> fieldType = field.getType();
            if (field.getName().equals("id") || field.getName().equals("gmtUpdate") || field.getName().equals("gmtCreate")) {
                continue;
            }
            //
            ApiField apiField = field.getAnnotation(ApiField.class);
            NotNull notNull = field.getAnnotation(NotNull.class);
            TableField tableField = field.getAnnotation(TableField.class);
            String fieldName = (tableField != null && StringUtils.isNotEmpty(tableField.value())) ? tableField.value().replace("`","") : field.getName();
            if (fieldType == Long.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` bigint(20)");
            } else if (fieldType == String.class) {
                TextFormat textFormat = field.getAnnotation(TextFormat.class);
                int length = 255;
                if (textFormat != null && textFormat.lengthMax() != Integer.MAX_VALUE) {
                    length = textFormat.lengthMax();
                }
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` ");
                if (length < 16383) {
                    sb.append("varchar(");
                    sb.append(length);
                    sb.append(")");
                } else if (length < 16777215) {
                    sb.append("TEXT");
                } else {
                    sb.append("MEDIUMTEXT");
                }
            } else if (fieldType == Integer.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` int(11)");
            } else if (fieldType == Boolean.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` bit(1)");
            } else if (fieldType == Date.class || fieldType == LocalDateTime.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` datetime");
            } else if (fieldType == LocalDate.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` date");
            } else if (fieldType == BigDecimal.class || fieldType == Float.class || fieldType == Double.class) {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` decimal(30,20)");
            } else {
                sb.append("`");
                sb.append(StringUtils.toUnderlineCase(fieldName));
                sb.append("` ");
                String type = "varchar(255)";
                if (tableField != null) {
                    Class<? extends TypeHandler> typeHandler = tableField.typeHandler();
                    if (typeHandler != UnknownTypeHandler.class) {
                        try {
                            Constructor<? extends TypeHandler> declaredConstructor = typeHandler.getDeclaredConstructor();
                            TypeHandler<?> instance = declaredConstructor.newInstance();
                            if (instance instanceof TableInitDataType) {
                                type = ((TableInitDataType) instance).dataType();
                            }
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            logger.warn("[Table Init] No Method or Constructor");
                        }
                    }
                }
                sb.append(type);
            }

            // 添加非空校验与注释
            if (notNull != null) {
                sb.append(" NOT NULL");
            }
            if (apiField != null) {
                sb.append(" COMMENT '");
                sb.append(apiField.description());
                if (apiField.enums() != null && apiField.enums() != EmptyEnums.class) {
                    sb.append(":");
                    String keyValue = BaseEnums.getKeyValue(apiField.enums());
                    sb.append(keyValue);
                }
                sb.append("'");
            }
            sb.append(",\n");
        }
        sb.append("`gmt_update` datetime NOT NULL,\n" +
                "`gmt_create` datetime NOT NULL,");
        sb.append("PRIMARY KEY (`id`)\n");
        sb.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;");
        return sb.toString();
    }

}
