package com.application.presentation.table.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.table.dto.request.TableCreateRequest;
import com.application.presentation.table.dto.request.TableModifyRequest;
import com.application.presentation.table.dto.response.TableIdResponse;
import com.application.presentation.table.dto.response.TableInfoResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@Schema(description = "테이블 API")
public interface TableApi {

	@Operation(
		summary = "테이블 생성",
		description = "가게에 테이블을 생성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = TableIdResponse.class))
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = TableIdResponse.class,
			description = "테이블 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_TABLE_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.EXIST_TABLE),
		}
	)
	ResponseEntity<ResponseBody<TableIdResponse>> createTable(
		@Parameter(hidden = true) final UserPassport userPassport,
		@RequestBody @Valid TableCreateRequest tableCreateRequest);

	@Operation(
		summary = "테이블 수 수정",
		description = "가게에 테이블을 정보를 수정합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "테이블 정보 수정 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_TABLE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_TABLE_WRITE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.EXIST_TABLE),
		}
	)
	ResponseEntity<ResponseBody<Void>> updateTable(
		@Parameter(hidden = true) final UserPassport userPassport,
		@RequestBody @Valid TableModifyRequest tableModifyRequest);

	@Operation(
		summary = "테이블 삭제",
		description = "가게에 테이블을 삭제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "테이블 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_TABLE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.STORE_IS_OPEN_TABLE_WRITE),
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteTable(
		final UserPassport userPassport,
		@Schema(description = "테이블 고유 ID", example = "123e4567-e89b-12d3-a456-426614174000")
		@RequestParam UUID tableId);

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
