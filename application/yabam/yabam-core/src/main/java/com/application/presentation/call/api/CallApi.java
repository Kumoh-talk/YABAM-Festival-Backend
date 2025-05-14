package com.application.presentation.call.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.call.dto.request.CallCreateRequest;
import com.application.presentation.call.dto.response.CallCursorResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

public interface CallApi {

	@Operation(
		summary = "호출 요청 API",
		description = "사용자가 호출을 요청합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "호출 요청 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CONFLICT_CLOSE_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.TABLE_NOT_ACTIVE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_NOT_MATCH)
		}
	)
	ResponseEntity<ResponseBody<Void>> postCall(
		@RequestBody @Valid CallCreateRequest callCreateRequest);

	@Operation(
		summary = "호출 완료 처리 API",
		description = "점주가 호출을 완료 처리합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "호출 완료 처리 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_CALL),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_VALID_CALL_OWNER)
		}
	)
	ResponseEntity<ResponseBody<Void>> completeCall(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "호출 ID", example = "1") @RequestParam Long callId);

	@Operation(
		summary = "미완료 호출 목록 조회 API",
		description = "점주가 자신의 가게에 들어온 미완료 호출을 커서 방식으로 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = CallCursorResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = CallCursorResponse.class,
			description = "미완료 호출 목록 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<CallCursorResponse>> getNonCompleteCalls(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "판매 ID", example = "1") @RequestParam Long saleId,
		@Schema(description = "마지막 호출 ID (커서)", example = "10") @RequestParam Long lastCallId,
		@Schema(description = "가져올 데이터 수", example = "20") @RequestParam Integer size);
}

