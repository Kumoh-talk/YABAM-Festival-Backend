package com.application.presentation.receipt.api;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.global.response.GlobalPageResponse;
import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.receipt.dto.response.ReceiptAndOrdersResponse;
import com.application.presentation.receipt.dto.response.ReceiptIdResponse;
import com.application.presentation.receipt.dto.response.ReceiptInfoResponse;
import com.application.presentation.receipt.dto.response.ReceiptResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public interface ReceiptApi {

	@Operation(
		summary = "테이블 영수증 등록 API",
		description = "대상 테이블의 영수증을 등록합니다." + " 먼저 대상 테이블에 미정산 된 영수증 id를 조회한 후, 존재하지 않는다면 해당 API로 영수증을 등록해야합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReceiptResponse.class,
			description = "영수증 등록 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_SALE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_TABLE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ALREADY_ACTIVE_TABLE)

		}
	)
	ResponseEntity<ResponseBody<ReceiptResponse>> registerReceipt(
		@RequestParam @NotNull Long storeId, @RequestParam @NotNull Long tableId);

	@Operation(
		summary = "영수증 세부정보 조회 API",
		description = "영수증의 세부정보르 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReceiptInfoResponse.class,
			description = "영수증 세부정보 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND)
		}
	)
	ResponseEntity<ResponseBody<ReceiptInfoResponse>> getReceiptInfo(@PathVariable Long receiptId);

	@Operation(
		summary = "영업 별 정산 영수증 페이지 조회 API",
		description = "대상 영업 별 영수증 페이지를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = GlobalPageResponse.class,
			description = "영수증 페이지 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_SALE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_STORE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_EQUAL_STORE_OWNER),
		}
	)
	ResponseEntity<ResponseBody<GlobalPageResponse<ReceiptInfoResponse>>> getAdjustedReceiptPageBySale(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long saleId,
		@Schema(description = "page, size 입력 가능") @ParameterObject
		@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

	@Operation(
		summary = "영수증 사용종료 API",
		description = "대상 영수증의 사용을 종료하고, 누적 주문 및 주문 메뉴를 응답합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptAndOrdersResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReceiptAndOrdersResponse.class,
			description = "영수증 사용종료 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED)
		}
	)
	ResponseEntity<ResponseBody<List<ReceiptAndOrdersResponse>>> stopReceiptUsage(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds);

	@Operation(
		summary = "영수증 사용 재시작 API",
		description = "대상 영수증의 사용을 재시작 합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "영수증 사용 재시작 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED)
		}
	)
	ResponseEntity<ResponseBody<Void>> restartReceiptUsage(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds);

	@Operation(
		summary = "영수증 최종 정산 API",
		description = "대상 영수증을 최종 정산 처리 합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "영수증 정산 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ALREADY_ADJUSTMENT_RECEIPT)

		}
	)
	ResponseEntity<ResponseBody<Void>> adjustReceipts(
		@Parameter(hidden = true) UserPassport userPassport,
		@RequestParam @NotEmpty List<Long> receiptIds);

	@Operation(
		summary = "영수증 삭제 API",
		description = "대상 영수증을 삭제 처리 합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "영수증 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED)
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteReceipt(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long receiptId);

	@Operation(
		summary = "대상 테이블 미정산 영수증 id 조회 API"
			+ " 처음 QR을 통해 주문창에 진입할 경우 해당 API로 영수증이 있는지 확인 후 없으면(null 값) 등록 API를 호출해야합니다.",
		description = "대상 테이블의 미정산 영수증 Id를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptIdResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = ReceiptIdResponse.class,
			description = "영수증 id 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<ReceiptIdResponse>> getNonAdjustReceiptId(@PathVariable Long tableId);

	@Operation(
		summary = "고객 별 영수증 내역 무한스크롤 조회 API",
		description = "대상 고객의 영수증 사용 내역을 무한 스크롤로 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = ReceiptInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = GlobalSliceResponse.class,
			description = "영수증 내역 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.USER_ACCESS_DENIED)
		}
	)
	ResponseEntity<ResponseBody<GlobalSliceResponse<ReceiptInfoResponse>>> getCustomerReceiptSlice(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long customerId,
		@RequestParam @Min(1) int pageSize,
		@Schema(description = "이전 페이지 가장 마지막 ReceiptId(첫 페이지 조회 시 생략)")
		@RequestParam(required = false) Long lastReceiptId);
}
