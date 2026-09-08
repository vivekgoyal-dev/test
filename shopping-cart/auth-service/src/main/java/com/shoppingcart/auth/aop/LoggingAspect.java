package com.shoppingcart.auth.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logs every controller and service call. The auth controller is excluded on purpose: its
 * arguments are the email and the plain-text password, which must never reach a log file.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("(execution(* com.shoppingcart.auth.controller..*(..)) "
            + "|| execution(* com.shoppingcart.auth.service..*(..))) "
            + "&& !execution(* com.shoppingcart.auth.controller.AuthController.register(..)) "
            + "&& !execution(* com.shoppingcart.auth.controller.AuthController.login(..)) "
            + "&& !execution(* com.shoppingcart.auth.service.AuthService*.register(..)) "
            + "&& !execution(* com.shoppingcart.auth.service.AuthService*.login(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName();
        long start = System.currentTimeMillis();
        log.info("{} called", method);
        try {
            Object result = joinPoint.proceed();
            log.info("{} returned in {}ms", method, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.error("{} threw {}: {}", method, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
