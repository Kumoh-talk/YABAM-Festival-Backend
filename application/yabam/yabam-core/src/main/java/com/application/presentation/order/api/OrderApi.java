package com.application.presentation.order.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.order.dto.request.PostOrderMenuRequest;
import com.application.presentation.order.dto.response.OrderAndMenusResponse;
import com.application.presentation.order.dto.response.OrderInfoResponse;
import com.application.presentation.order.dto.response.OrderResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.order.entity.vo.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public interface OrderApi {

	@Operation(
		summary = "장바구니 기반 주문 생성 API",
		description = "장바구니에 담긴 메뉴를 기반으로 주문을 생성합니다." + " 장바구니가 비었으면 주문을 실패합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderResponse.class,
			description = "주문 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CLOSE_SALE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CART_EMPTY),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_SOLD_OUT)

		}
	)
	ResponseEntity<ResponseBody<OrderResponse>> postOrderWithCart(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable UUID receiptId);

	@Operation(
		summary = "메뉴 리스트 기반 주문 생성 API",
		description = "직접 전달된 메뉴를 기반으로 주문을 생성합니다." + " 점주 전용 api 입니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderResponse.class,
			description = "주문 생성 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.CLOSE_SALE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_SOLD_OUT)

		}
	)
	ResponseEntity<ResponseBody<OrderResponse>> postOrderWithoutCart(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable UUID receiptId, @RequestBody @Valid @NotEmpty List<PostOrderMenuRequest> postOrderMenuRequests);

	@Operation(
		summary = "주문 상태 변경 API",
		description = "전달된 상태로 주문 상태를 변경합니다." + " 주문 취소, 접수가 가능합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderInfoResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderInfoResponse.class,
			description = "주문 상태 변경 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.INVALID_STATE_TRANSITION)

		}
	)
	ResponseEntity<ResponseBody<OrderInfoResponse>> patchOrderStatus(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long orderId,
		@Schema(description = "주문 상태(RECEIVED(수령), CANCELED(취소)") @RequestParam @NotNull OrderStatus orderStatus);

	@Operation(
		summary = "대상 영수증 주문 리스트 조회 API",
		description = "대상 영수증의 주문 리스트를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderAndMenusResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderAndMenusResponse.class,
			description = "주문 리스트 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_NOT_FOUND)

		}
	)
	ResponseEntity<ResponseBody<List<OrderAndMenusResponse>>> getReceiptOrders(@PathVariable UUID receiptId);

	@Operation(
		summary = "영업별 주문 리스트 조회 API",
		description = "전달된 상태의 영업 별 주문 리스트를 조회합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = GlobalSliceResponse.class,
			description = "주문 리스트 조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.NOT_FOUND_SALE),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.SALE_ACCESS_DENIED)

		}
	)
	ResponseEntity<ResponseBody<GlobalSliceResponse<OrderResponse>>> getSaleOrderSlice(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long saleId,
		@Schema(description = "주문 상태(ORDERED(접수 전), RECEIVED(접수), CANCELED(취소), COMPLETED(완료))",
			example = "ORDERED, RECEIVED")
		@RequestParam @NotEmpty List<OrderStatus> orderStatuses,
		@RequestParam @Min(value = 1, message = "페이지 크기는 최소 1 이상입니다.") int pageSize,
		@RequestParam(required = false) Long lastOrderId);

	@Operation(
		summary = "주문 상세조회 API",
		description = "주문을 상세조회 합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderAndMenusResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderAndMenusResponse.class,
			description = "주문 상세조회 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_NOT_FOUND)

		}
	)
	ResponseEntity<ResponseBody<OrderAndMenusResponse>> getOrder(@PathVariable Long orderId);
}
