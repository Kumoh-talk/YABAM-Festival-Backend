package com.auth.presentation.controller;

import static com.response.ResponseUtil.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.domain.service.FakeUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.http.HttpHeaderName;
import com.response.ResponseBody;
import com.vo.UserPassport;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Profile({"local", "dev"})
@RestController
@RequestMapping("/api/login/fake")
@RequiredArgsConstructor
public class FakeAuthController {

	private final FakeUserService fakeUserService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping("/user")
	public ResponseEntity<ResponseBody<Void>> fakeUserLogin(
		HttpServletResponse response) throws JsonProcessingException {

		UserPassport userPassport = fakeUserService.fakeUserLogin();
		serializeUserPassport(response, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@PostMapping("/owner")
	public ResponseEntity<ResponseBody<Void>> fakeOwnerLogin(
		HttpServletResponse response) throws JsonProcessingException {

		UserPassport userPassport = fakeUserService.fakeOwnerLogin();
		serializeUserPassport(response, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	private void serializeUserPassport(HttpServletResponse response, UserPassport userPassport) throws
		JsonProcessingException {
		// 직렬화 및 인코딩
		String userPassportJson = objectMapper.writeValueAsString(userPassport);
		String encodedPassport = URLEncoder.encode(userPassportJson, StandardCharsets.UTF_8);

		// 헤더에 추가
		response.setHeader(HttpHeaderName.RESPONSE_USER_INFO_HEADER, encodedPassport);
	}
}
