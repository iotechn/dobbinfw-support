//package com.dobbinsoft.fw.support.config.db.syntax;
//
//import org.apache.ibatis.executor.statement.StatementHandler;
//import org.apache.ibatis.plugin.*;
//
//import java.lang.reflect.Field;
//import java.sql.Connection;
//import java.util.Properties;
//
//@Intercepts({
//    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
//})
//public class SyntaxInterceptor implements Interceptor {
//
//    public static final ThreadLocal<Boolean> SWITCH = new ThreadLocal<>();
//
//    @Override
//    public Object intercept(Invocation invocation) throws Throwable {
//        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
//        // 获取原始 SQL
//        String originalSql = statementHandler.getBoundSql().getSql();
//
//        // 进行 SQL 修改操作
//        String modifiedSql = modifySql(originalSql);
//
//        // 反射修改 SQL
//        Field sqlField = statementHandler.getBoundSql().getClass().getDeclaredField("sql");
//        sqlField.setAccessible(true);
//        sqlField.set(statementHandler.getBoundSql(), modifiedSql);
//
//        return invocation.proceed();
//    }
//
//    private String modifySql(String originalSql) {
//        // 这里编写 SQL 修改逻辑，例如添加 WHERE 条件
//        Boolean b = SWITCH.get();
//        if (b != null && b) {
//            return "EXPLAIN " + originalSql;
//        } else {
//            return originalSql;
//        }
//    }
//
//    @Override
//    public Object plugin(Object target) {
//        return Plugin.wrap(target, this);
//    }
//
//    @Override
//    public void setProperties(Properties properties) {
//        // 可以在这里读取配置文件中的属性
//    }
//}
