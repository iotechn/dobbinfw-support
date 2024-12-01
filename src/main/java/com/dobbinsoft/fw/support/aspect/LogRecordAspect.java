package com.dobbinsoft.fw.support.aspect;

import com.dobbinsoft.fw.core.annotation.LogRecord;
import com.dobbinsoft.fw.support.log.LogRecordContext;
import com.dobbinsoft.fw.support.log.LogRecordPersistent;
import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import com.dobbinsoft.fw.support.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@Aspect
public class LogRecordAspect {

    @Autowired(required = false)
    private LogRecordPersistent logRecordPersistent;

    @Pointcut("@annotation(com.dobbinsoft.fw.core.annotation.LogRecord)")
    public void logPointCut() {}


    @Around("logPointCut()")
    public Object doLog(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LogRecord logRecord = method.getAnnotation(LogRecord.class);

        ExpressionParser parser = new SpelExpressionParser();
        StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();
        String[] params = discoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        EvaluationContext context = new StandardEvaluationContext();

        for (int len = 0; len < Objects.requireNonNull(params).length; len++) {
            context.setVariable(params[len], args[len]);
        }

        try {
            Object proceed = joinPoint.proceed();
            try {
                Map<String, Object> contextMap = LogRecordContext.get();
                if (CollectionUtils.isNotEmpty(contextMap)) {
                    contextMap.forEach(context::setVariable);
                }
                context.setVariable("return", proceed);
                String value = parser.parseExpression(logRecord.value(), new TemplateParserContext()).getValue(context, String.class);
                log.info("[业务日志] success={}", value);
                if (logRecordPersistent != null) {
                    LogRecordContext.LogRefer refer = LogRecordContext.getRefer();
                    logRecordPersistent.write(value, true, refer);
                }
            } catch (Exception e) {
                Map<String, Object> customContext = LogRecordContext.get();
                log.error("[业务日志] 异常上下文： CustomContext={}", (customContext == null ? "{}" : JacksonUtil.toJSONString(customContext)));
                log.error("[业务日志] 异常", e);
            }
            return proceed;
        } catch (Throwable e) {
            try {
                if (StringUtils.isNotEmpty(logRecord.failValue())) {
                    context.setVariable("return", e);
                    String value = parser.parseExpression(logRecord.failValue(), new TemplateParserContext()).getValue(context, String.class);
                    Map<String, Object> customContext = LogRecordContext.get();
                    log.error("[业务日志] 异常上下文： CustomContext={}", (customContext == null ? "{}" : JacksonUtil.toJSONString(customContext)));
                    log.info("[业务日志] fail={}", value);
                    if (logRecordPersistent != null) {
                        logRecordPersistent.write(value, false, LogRecordContext.getRefer());
                    }
                }
            } catch (Exception ex) {
                // 不影响主流程
            }
            throw e;
        } finally {
            LogRecordContext.clear();
        }
    }

}
