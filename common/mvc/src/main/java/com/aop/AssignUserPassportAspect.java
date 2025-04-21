package com.aop;

import java.lang.reflect.Parameter;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.interceptor.DeserializingUserPassportInterceptor;

import domain.pos.member.entity.UserPassport;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
public class AssignUserPassportAspect {

	@Around("@annotation(com.authorization.AssignUserPassport)")
	public Object assignUserPassport(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		String attributeName = DeserializingUserPassportInterceptor.USER_INFO_ATTRIBUTE;
		UserPassport userPassport = (UserPassport)request.getAttribute(attributeName);

		Object[] argValues = proceedingJoinPoint.getArgs();
		MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
		Parameter[] parameters = signature.getMethod().getParameters();

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].getType() == UserPassport.class) {
				argValues[i] = userPassport;
				break;
			}
		}

		return proceedingJoinPoint.proceed(argValues);
	}
}
