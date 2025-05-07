package com.application.presentation.table.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.table.dto.TableInfoResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;

import domain.pos.member.entity.UserPassport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Schema(description = "테이블 API")
public interface TableApi {

	@Operation(
		summary = "테이블 생성",
		description = "가게에 테이블을 생성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = TableInfoResponse.class))
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = TableInfoResponse.class,
			description = "테이블 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_TABLE_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.EXIST_TABLE),
		}
	)
	ResponseEntity<ResponseBody<TableInfoResponse>> createTable(
		@Parameter(hidden = true) final UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam Long storeId,
		@Schema(description = "테이블 수", example = "5") @RequestParam Integer tableNumber);

	@Operation(
		summary = "테이블 수 수정",
		description = "가게에 테이블을 수를 수정합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = TableInfoResponse.class))
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = TableInfoResponse.class,
			description = "테이블 수 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_TABLE_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.TABLE_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.TABLE_NOT_EQUAL_MODIFY),
		}
	)
	ResponseEntity<ResponseBody<TableInfoResponse>> updateTableNum(
		@Parameter(hidden = true) final UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam Long storeId,
		@Schema(description = "테이블 수", example = "10") @RequestParam Integer tableNumber);

	@Operation(
		summary = "테이블 현황 조회 API",
		description = "가게에 테이블 현황을 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = TableInfoResponse.class))
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = TableInfoResponse.class,
			description = "테이블 현황 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<TableInfoResponse>> getTables(
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam Long storeId);
}
