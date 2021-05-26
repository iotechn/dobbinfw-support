package com.dobbinsoft.fw.support.aspect;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dobbinsoft.fw.support.annotation.Query;
import com.dobbinsoft.fw.support.annotation.QueryCondition;
import com.dobbinsoft.fw.support.context.QueryContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * ClassName: QueryWrapperAspect
 * Description: 构建查询条件切面
 *
 * @author: rize
 * @date: 2021-04-21
 */
@Aspect
@Component
public class QueryWrapperAspect {

    @Pointcut("@annotation(com.dobbinsoft.fw.support.annotation.Query)")
    public void wrapperPointCut() {
    }

    @Before("wrapperPointCut()")
    public void before(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Query query = method.getAnnotation(Query.class);
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();
        QueryWrapper wrapper = new QueryWrapper();
        for (int i = 0; i < parameters.length; i++) {
            if (!ObjectUtils.isEmpty(args[i])) {
                Parameter parameter = parameters[i];
                QueryCondition queryCondition = parameter.getAnnotation(QueryCondition.class);
                if (queryCondition != null) {
                    String field;
                    if (!"".equals(queryCondition.field())) {
                        field = queryCondition.field();
                    } else {
                        field = parameter.getName();
                    }
                    switch (queryCondition.condition()) {
                        case EQUAL:
                            wrapper.eq(field, args[i]);
                            break;
                        case LIKE:
                            wrapper.like(field, args[i]);
                            break;
                        case GT:
                            wrapper.gt(field, args[i]);
                            break;
                        case LT:
                            wrapper.lt(field, args[i]);
                            break;
                        case GE:
                            wrapper.ge(field, args[i]);
                            break;
                        case LE:
                            wrapper.le(field, args[i]);
                            break;
                    }
                }
            }
        }
        if (query.isAsc()) {
            wrapper.orderByAsc(query.sort());
        } else {
            wrapper.orderByDesc(query.sort());
        }
        QueryContext.set(wrapper);
    }

}
