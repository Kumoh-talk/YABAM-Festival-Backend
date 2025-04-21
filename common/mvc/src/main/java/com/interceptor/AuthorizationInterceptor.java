package com.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.authorization.HasRole;
import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.member.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod handlerMethod) {
			HasRole hasRole = handlerMethod.getMethodAnnotation(HasRole.class);

			if (hasRole != null) {
				UserRole requiredRole = hasRole.userRole();
				String attributeName = DeserializingUserPassportInterceptor.USER_INFO_ATTRIBUTE;

				UserPassport userPassport = (UserPassport)request.getAttribute(attributeName);

				if (userPassport == null || !userPassport.getUserRole().isHigherOrEqual(requiredRole)) {
					log.warn("요청 접근 권한이 없습니다. 필요 역할 : {}", requiredRole);
					throw new ServiceException(ErrorCode.ACCESS_DENIED);
				}
			}
		}
		return true;
	}
}
