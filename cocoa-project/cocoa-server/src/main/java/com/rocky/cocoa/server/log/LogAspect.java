package com.rocky.cocoa.server.log;

import cn.hutool.core.date.DateUtil;
import com.google.common.base.Joiner;
import com.rocky.cocoa.entity.system.OperationLog;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.service.OperationLogService;
import org.apache.parquet.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private OperationLogService operationLogService;

    private final String operationLogPoint = "@annotation(com.rocky.cocoa.server.log.OperationRecord)";

    @Pointcut(operationLogPoint)
    public void operationLogPoint() {

    }

    @Before("operationLogPoint()")
    public void recordOperation(JoinPoint joinPoint) throws Throwable {
        //获取operation record
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        OperationRecord operationRecord = method.getAnnotation(OperationRecord.class);


        //获取operation object
        Object[] args = joinPoint.getArgs();

        Parameter[] parameters = method.getParameters();
        List<String> operationObjs = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            Annotation[] annotations = parameters[i].getAnnotations();
            for (Annotation annotation : annotations) {
                String obj = null;
                if (annotation instanceof OperationObj) {
                    obj = ((OperationObj) annotation).value();
                }
                if (Strings.isNullOrEmpty(obj)) {
                    obj = args[i].toString();
                }
                operationObjs.add(obj);
                break;
            }
        }

        //将operation log 存储数据库

        OperationLog operationLog = new OperationLog();
        operationLog.setUser(ContextUtil.getCurrentUser().getName());
        operationLog.setOperation(operationRecord.value());
        operationLog.setObj(Joiner.on(" ").join(operationObjs));
        operationLog.setIsTrash(false);
        operationLog.setCreateTime(new Date());

        operationLogService.recordOperation(operationLog);

    }

}
