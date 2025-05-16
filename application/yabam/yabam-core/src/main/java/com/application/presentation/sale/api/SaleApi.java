package com.application.presentation.sale.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.sale.dto.response.SaleCursorResponse;
import com.application.presentation.sale.dto.response.SaleIdResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Schema(
	name = "SaleApi",
	description = "판매 API")
public interface SaleApi {

	@Operation(
		summary = "판매 시작",
		description = "판매를 시작합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = SaleIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = SaleIdResponse.class,
			description = "판매 시작 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CONFLICT_OPEN_STORE)
		}
	)
	ResponseEntity<ResponseBody<SaleIdResponse>> openStore(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam Long storeId
	);

	@Operation(
		summary = "판매 종료",
		description = "판매를 종료합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "판매 종료 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CONFLICT_CLOSE_STORE)
		}
	)
	ResponseEntity<ResponseBody<Void>> closeStore(
		@Parameter(hidden = true) UserPassport userPassport,
		@Schema(description = "가게 고유 ID", example = "1") @RequestParam Long storeId
	);

	@Operation(
		summary = "판매 내역 조회",
		description = "판매 내역을 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = SaleCursorResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = SaleCursorResponse.class,
			description = "판매 내역 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<SaleCursorResponse>> getSales(
		@Schema(description = "가게 고유 ID", example = "1")
		@RequestParam Long storeId,
		@Schema(description = "마지막 판매 ID", example = "1")
		@RequestParam(required = false) Long lastSaleId,
		@Schema(description = "가져올 데이터 수", example = "10")
		@RequestParam Integer size
	);
}
