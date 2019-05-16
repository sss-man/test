package com.db.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class SysCacheAspect {
	@Pointcut("@annotation(com.db.common.annotation.RequiredCache)")
	public void doCache() {
	}

	@Around("doCache()")
	public Object around(ProceedingJoinPoint jp) throws Throwable {
		System.out.println("query data");
		Object result = jp.proceed();
		System.out.println("put data");
		return result;
	}
	
	
	@Around("doCache()")
	public Object around2(ProceedingJoinPoint jp) throws Throwable {
		System.out.println("query data2");
		Object result = jp.proceed();
		System.out.println("put data2");
		return result;
	}
}
