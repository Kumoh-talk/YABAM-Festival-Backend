package com.interceptor;

import static com.interceptor.DeserializingUserPassportInterceptor.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.annotation.HasRole;
import com.vo.UserPassport;
import com.vo.UserRole;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class InterceptorTestHelper {

	@GetMapping("/test/userPassport")
	public ResponseEntity<UserPassport> deserializingUserPassportTest(HttpServletRequest request) {
		ResponseEntity<UserPassport> result = ResponseEntity.ok(
			(UserPassport)request.getAttribute(USER_INFO_ATTRIBUTE));
		return result;
	}

	@HasRole
	@GetMapping("/test/hasRole/user")
	public ResponseEntity<UserRole> userRoleTest(HttpServletRequest request) {
		UserPassport userPassport = (UserPassport)request.getAttribute(USER_INFO_ATTRIBUTE);
		return ResponseEntity.ok(userPassport.getUserRole());
	}

	@HasRole(userRole = UserRole.ROLE_OWNER)
	@GetMapping("/test/hasRole/owner")
	public ResponseEntity<UserRole> ownerRoleTest(HttpServletRequest request) {
		UserPassport userPassport = (UserPassport)request.getAttribute(USER_INFO_ATTRIBUTE);
		return ResponseEntity.ok(userPassport.getUserRole());
	}
}
