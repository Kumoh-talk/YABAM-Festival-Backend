package com.application.presentation.cart.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.cart.dto.response.CartInfoResponse;
import com.application.presentation.cart.dto.response.OrderSessionResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Min;

@Schema(description = "장바구니 API")
public interface CartApi {

	@Operation(
		summary = "장바구니 상품 추가/수정 API",
		description = "장바구니에 상품을 추가하거나 수량을 수정합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "장바구니 상품 추가/수정 성공"
		),
		errors = @ApiErrorResponseExplanation(
			errorCode = ErrorCode.MENU_NOT_FOUND
		)
	)
	ResponseEntity<ResponseBody<Void>> postCart(
		@Schema(description = "영수증 ID", example = "1") @RequestParam final UUID receiptId,
		@Schema(description = "메뉴 ID", example = "1") @RequestParam final Long menuId,
		@Schema(description = "수량", example = "2")
		@RequestParam @Min(value = 1, message = "수량은 최소 1개 이상입니다.") final Integer quantity);

	@Operation(
		summary = "장바구니 상품 삭제 API",
		description = "장바구니에서 특정 메뉴를 삭제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "장바구니 상품 삭제 성공"
		)
	)
	ResponseEntity<ResponseBody<Void>> deleteCart(
		@Schema(description = "영수증 ID", example = "1") @RequestParam final UUID receiptId,
		@Schema(description = "메뉴 ID", example = "1") @RequestParam final Long menuId);

	@Operation(
		summary = "장바구니 조회 API",
		description = "특정 영수증 ID에 해당하는 장바구니 정보를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = CartInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = CartInfoResponse.class,
			description = "장바구니 조회 성공"
		)
	)
	ResponseEntity<ResponseBody<CartInfoResponse>> getCart(
		@Schema(description = "영수증 ID", example = "1") @RequestParam final UUID receiptId);

	@Operation(
		summary = "주문 대기 세션 진입 API",
		description = "장바구니를 잠그고 최신 장바구니 내용과 세션 토큰을 반환합니다. 세션 진입 중에는 장바구니 수정이 불가합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderSessionResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderSessionResponse.class,
			description = "세션 진입 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_ORDER_SESSION_ACTIVE)
		}
	)
	ResponseEntity<ResponseBody<OrderSessionResponse>> enterOrderSession(
		@Schema(description = "영수증 ID", example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID receiptId);

	@Operation(
		summary = "주문 대기 세션 취소 API",
		description = "주문 대기 세션을 취소하고 장바구니 잠금을 해제합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "세션 취소 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_ORDER_SESSION_INVALID)
		}
	)
	ResponseEntity<ResponseBody<Void>> cancelOrderSession(
		@Schema(description = "영수증 ID", example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID receiptId,
		@Parameter(description = "세션 토큰 헤더", required = true)
		@RequestHeader("X-Order-Session-Token") UUID sessionToken);
}
