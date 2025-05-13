package com.auth.presentation.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.domain.service.UserService;
import com.auth.presentation.api.UserApi;
import com.auth.presentation.dto.response.UserInfoResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController implements UserApi {

	private final UserService userService;

	@GetMapping()
	@HasRole(userRole = ROLE_USER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<UserInfoResponse>> getUserInfo(UserPassport userPassport) {
		UserPassport userInfo = userService.getUserInfo(userPassport.getUserId());
		return ResponseEntity.ok(createSuccessResponse(UserInfoResponse.of(
			userInfo.getUserId(),
			userInfo.getUserNickname(),
			userInfo.getUserRole()
		)));
	}
}
