package com.example.demo_jour_3.demo_aspect.aspect;

import com.example.demo_jour_3.entity.MetricEntry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class PerformanceAspect {
    private final ConcurrentHashMap<String, MetricEntry> metrics = new ConcurrentHashMap<String, MetricEntry>();

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {}

    @Pointcut("!within(com.example.demo_jour_3.controller.MetricsController)")
    public void notMetricsController() {}
    @Pointcut("@annotation(com.example.demo_jour_3.demo_aspect.annotation.Tracked)")
    public void trackedMethods(){}

    @Around("restControllerMethods() && notMetricsController()")
    public Object measureController(ProceedingJoinPoint joinPoint) throws Throwable {
        return measure(joinPoint);
    }

    @Around("trackedMethods() && !restControllerMethods()")
    public Object measureTracked(ProceedingJoinPoint joinPoint) throws Throwable {
        return measure(joinPoint);
    }

    private Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed(joinPoint.getArgs());
        long duration = System.currentTimeMillis() - start;
        metrics.computeIfAbsent(className + "."+methodName, k -> new MetricEntry()).record(duration);
        return result;
    }

    public Map<String, Map<String, Object>> getMetrics() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        metrics.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> result.put(e.getKey(), e.getValue().toMap()));
        return result;
    }

    public void resetMetrics() {
        metrics.clear();
    }
}
