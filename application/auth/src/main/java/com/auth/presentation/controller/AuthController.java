package com.auth.presentation.controller;

import static com.response.ResponseUtil.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.domain.entity.OidcPayload;
import com.auth.domain.implement.OAuthOidcHelper;
import com.auth.domain.service.UserService;
import com.auth.presentation.api.AuthApi;
import com.auth.presentation.dto.request.OidcLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.http.HttpHeaderName;
import com.response.ResponseBody;
import com.vo.UserPassport;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class AuthController implements AuthApi {
	private final UserService userService;
	private final OAuthOidcHelper oAuthOidcHelper;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostMapping()
	public ResponseEntity<ResponseBody<Void>> login(
		HttpServletResponse response,
		@RequestBody @Valid OidcLoginRequest request) throws JsonProcessingException {
		// 1. ID Token에서 페이로드 추출
		OidcPayload payload = oAuthOidcHelper.getPayload(
			request.provider(),
			request.oauthId(),
			request.idToken(),
			request.nonce()
		);

		// 2. 로그인 / 회원가입
		UserPassport userPassport = userService.findOrCreateUser(
			payload.sub(),
			payload.email(),
			request.provider()
		);

		// 직렬화 및 인코딩
		String userPassportJson = objectMapper.writeValueAsString(userPassport);
		String encodedPassport = URLEncoder.encode(userPassportJson, StandardCharsets.UTF_8);

		// 헤더에 추가
		response.setHeader(HttpHeaderName.RESPONSE_USER_INFO_HEADER, encodedPassport);

		return ResponseEntity.ok(createSuccessResponse());
	}
}
