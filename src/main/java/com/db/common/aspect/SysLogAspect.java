package com.db.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class SysLogAspect {
	private Logger log = LoggerFactory.getLogger(SysLogAspect.class);
	@Pointcut("bean(sysLogServiceImpl)")
	public void doLog(){
		
	}
	
	@Around("doLog()")
	public Object around(ProceedingJoinPoint joinPoint) 
				throws Throwable{
        System.out.println("方法开始执行");
		Object result = joinPoint.proceed();
		System.out.println("方法结束执行");
		return result;
	}
	

}
