package com.dobbinsoft.fw.support.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SPELUtils {

    private static final Logger logger = LoggerFactory.getLogger(SPELUtils.class);

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 解析 spel 表达式
     * @param method
     * @param arguments
     * @param spel
     * @return
     */
    public static Object parseSpel(Method method, Object[] arguments, String spel) {
        return parseSpel(method, arguments, spel, null);
    }

    /**
     * 解析 spel 表达式
     * @param method    方法
     * @param arguments 参数
     * @param spel      表达式
     * @param result    方法执行后的返回值
     * @return 执行spel表达式后的结果
     */
    public static Object parseSpel(Method method, Object[] arguments, String spel, Object result) {
        List<String> params = Arrays.stream(method.getParameters()).map(item -> item.getName()).collect(Collectors.toList());
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < params.size(); i++) {
            context.setVariable(params.get(i), arguments[i]);
        }
        if (result != null) {
            context.setVariable("return", result);
        }
        try {
            Expression expression = PARSER.parseExpression(spel);
            Object value = expression.getValue(context, Object.class);
            return value;
        } catch (Exception e) {
            logger.error("[解析SPEL 表达式] 异常", e);
            return null;
        }
    }

    public static String parseKey(Method method, Object[] arguments,String spel) {
        Object o = parseSpel(method, arguments, spel);
        return o == null ? null : o.toString();
    }

}
