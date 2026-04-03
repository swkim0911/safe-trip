package com.swkim.safetrip.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    private static final long SLOW_THRESHOLD_MS = 500;

    @Around("execution(* com.swkim.safetrip.service.*.*(..))")
    public Object logSlowMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration > SLOW_THRESHOLD_MS) {
                log.warn("Slow method: {}.{}() duration={}ms",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        joinPoint.getSignature().getName(),
                        duration);
            }
        }
    }
}
