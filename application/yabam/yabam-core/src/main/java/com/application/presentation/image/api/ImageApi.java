package com.application.presentation.image.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.image.dto.request.PresignedUrlRequest;
import com.application.presentation.image.dto.response.PresignedUrlResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

public interface ImageApi {
	@Operation(
		summary = "가게 관련 데이터 이미지 조회용 Presigned URL 발급 API",
		description = "가게 ID와 이미지 프로퍼티를 전달받아, S3 접근을 위한 Presigned URL을 발급합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = PresignedUrlResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "Presigned URL 발급 성공",
			responseClass = PresignedUrlResponse.class
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER)
		}
	)
	ResponseEntity<ResponseBody<PresignedUrlResponse>> getPresignedUrl(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestBody @Valid PresignedUrlRequest presignedUrlRequest);
}
