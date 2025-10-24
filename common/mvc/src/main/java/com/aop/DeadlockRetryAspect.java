package com.aop;

import java.util.concurrent.ThreadLocalRandom;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.dao.CannotAcquireLockException;

@Aspect
@Order(0)
public class DeadlockRetryAspect {
	private static final long BASE_DELAY = 200L;

	@Around("@annotation(com.annotation.DeadlockRetry)")
	public Object retryOnDeadlock(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		int maxAttempts = 3;
		int attempts = 0;

		while (true) {
			try {
				return proceedingJoinPoint.proceed();
			} catch (CannotAcquireLockException e) {
				if (++attempts >= maxAttempts) {
					throw e;
				}

				long delay = (long)(BASE_DELAY * Math.pow(2, attempts - 1))
					+ ThreadLocalRandom.current().nextLong(50L, 100L);
				Thread.sleep(delay);
			}
		}
	}
}
