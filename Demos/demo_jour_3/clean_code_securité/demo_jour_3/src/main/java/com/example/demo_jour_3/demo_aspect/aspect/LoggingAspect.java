package com.example.demo_jour_3.demo_aspect.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    @Around("@annotation(com.example.demo_jour_3.demo_aspect.annotation.Loggable)")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        logger.info("Début de la méthode : " + methodName);
        Object result = joinPoint.proceed(args);
        logger.info("Fin de méthode : "+ methodName);
        return  result;
    }
}
