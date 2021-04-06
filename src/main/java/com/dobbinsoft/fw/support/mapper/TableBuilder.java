//package com.dobbinsoft.fw.support.mapper;
//
//import cn.hutool.core.util.StrUtil;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
///**
// * ClassName: TableBuilder
// * Description: 根据IoC中的Mapper实例，的DO泛型定义，自定输出建表SQL
// *
// * @author: e-weichaozheng
// * @date: 2021-03-31
// */
//public class TableBuilder {
//
//
//    /**
//     * CREATE TABLE IF NOT EXISTS `unierp_erp_sku` (
//     * `id` bigint(20) NOT NULL AUTO_INCREMENT,
//     * `code` varchar(255) NOT NULL,
//     * `bar_code` varchar(255) DEFAULT NULL,
//     * `title` varchar(255) NOT NULL,
//     * `img` varchar(255) DEFAULT NULL,
//     * `category_id` bigint(20) DEFAULT NULL,
//     * `type` int(11) NOT NULL,
//     * `specification` varchar(255) DEFAULT NULL,
//     * `band` varchar(255) DEFAULT NULL,
//     * `place` varchar(255) DEFAULT NULL,
//     * `unit` varchar(255) DEFAULT NULL,
//     * `purchase_unit` varchar(255) DEFAULT NULL,
//     * `sales_unit` varchar(255) DEFAULT NULL,
//     * `expiration_days` int(11) DEFAULT NULL,
//     * `warning_days` int(11) DEFAULT NULL,
//     * `location_id` bigint(20) DEFAULT NULL,
//     * `vendor_id` bigint(20) DEFAULT NULL,
//     * `buyer_id` bigint(20) DEFAULT NULL,
//     * `stock_min` int(11) DEFAULT NULL,
//     * `stock_max` int(11) DEFAULT NULL,
//     * `stock_warning` int(11) DEFAULT NULL,
//     * `gmt_update` datetime NOT NULL,
//     * `gmt_create` datetime NOT NULL,
//     * PRIMARY KEY (`id`)
//     * ) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4;
//     *
//     * @param iMapper
//     * @return
//     */
//
//    public String getCreateSql(IMapper iMapper, String prefix) {
//        Class<? extends IMapper> clazz = iMapper.getClass();
//        Type[] genericInterfaces = clazz.getGenericInterfaces();
//        for (Type type : genericInterfaces) {
//            if (type instanceof Class) {
//                Class mapperClazz = (Class) type;
//                Type[] iMapperTypes = mapperClazz.getGenericInterfaces();
//                if (iMapperTypes.length > 0) {
//                    for (Type t : iMapperTypes) {
//                        if (t instanceof ParameterizedType) {
//                            ParameterizedType pt = (ParameterizedType) t;
//                            if (IMapper.class.isAssignableFrom((Class) pt.getRawType())) {
//                                // 若是IMapper的子类
//                                Class entityClass = (Class) pt.getActualTypeArguments()[0];
//                                // 开始构建SQL
//                                Field[] declaredFields = entityClass.getDeclaredFields();
//                                StringBuilder sb = new StringBuilder();
//                                sb.append("CREATE TABLE IF NOT EXISTS `");
//                                sb.append(prefix);
//                                sb.append("_");
//                                String tableName = StrUtil.toUnderlineCase(entityClass.getSimpleName()).replace("_DO", "");
//                                sb.append(tableName);
//                                sb.append("` (\n");
//                                sb.append("`id` bigint(20) NOT NULL AUTO_INCREMENT,\n");
//                                for (Field field : declaredFields) {
//                                    Class<?> fieldType = field.getType();
//                                    sb.append("`");
//                                    sb.append(StrUtil.toUnderlineCase(field.getName()));
//                                    if (fieldType == Long.class) {
//                                        sb.append("` bigint(20) DEFAULT NULL,\n");
//                                    } else if (fieldType == String.class) {
//                                        sb.append("` varchar(255) NOT NULL,\n");
//                                    } else if (fieldType == Integer.class) {
//                                        sb.append("` int(11) NOT NULL,\n");
//                                    } else if (fieldType == Date.class) {
//                                        sb.append("` datetime NOT NULL,\n");
//                                    }
//                                }
//                                sb.append("`gmt_update` datetime NOT NULL,\n" +
//                                        "`gmt_create` datetime NOT NULL,");
//                                sb.append("PRIMARY KEY (`id`)\n");
//                                sb.append(") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;");
//                                return sb.toString();
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//}
