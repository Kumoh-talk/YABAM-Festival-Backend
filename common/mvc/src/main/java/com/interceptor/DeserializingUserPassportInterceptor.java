package com.interceptor;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.http.HttpHeaderName;
import com.vo.UserPassport;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class DeserializingUserPassportInterceptor implements HandlerInterceptor {

	private final ObjectMapper objectMapper = new ObjectMapper();
	public static final String USER_INFO_ATTRIBUTE = "userInfo";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String userInfoHeader = request.getHeader(HttpHeaderName.REQUEST_USER_INFO_HEADER);

		if (StringUtils.hasText(userInfoHeader)) {
			try {
				String decodedUserInfoHeader = URLDecoder.decode(userInfoHeader, StandardCharsets.UTF_8);
				UserPassport userPassport = objectMapper.readValue(decodedUserInfoHeader, UserPassport.class);
				request.setAttribute(USER_INFO_ATTRIBUTE, userPassport);
			} catch (IOException e) {
				request.setAttribute(USER_INFO_ATTRIBUTE, UserPassport.anonymous());
			}
		}

		return true;
	}
}
