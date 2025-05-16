package com.application.presentation.order.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.application.global.config.swagger.ApiErrorResponseExplanation;
import com.application.global.config.swagger.ApiResponseExplanations;
import com.application.global.config.swagger.ApiSuccessResponseExplanation;
import com.application.presentation.order.dto.request.PostOrderMenuRequest;
import com.application.presentation.order.dto.response.OrderMenuResponse;
import com.exception.ErrorCode;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.order.entity.vo.OrderMenuStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public interface OrderMenuApi {

	@Operation(
		summary = "주문 메뉴 상태 변경 API",
		description = "개별 주문 메뉴의 상태를 변경합니다." + " 재조리, 취소, 완료 상태로 변경 가능합니다."
			+ " \n 주문 상태가 RECEIVED 상태여야 합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderMenuResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderMenuResponse.class,
			description = "주문 메뉴 상태 변경 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_STATUS_NOT_RECEIVED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_ACCESS_DENIED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.INVALID_STATE_TRANSITION)

		}
	)
	ResponseEntity<ResponseBody<OrderMenuResponse>> patchOrderMenuStatus(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long orderMenuId,
		@Schema(description = "주문 메뉴 상태(COOKING(재조리), CANCELED(취소), COMPLETED(완료))")
		@RequestParam @NotNull OrderMenuStatus orderMenuStatus);

	@Operation(
		summary = "주문 메뉴 추가 API",
		description = "주문 메뉴를 추가합니다."
			+ " \n 주문 상태가 RECEIVED 상태여야 합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderMenuResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderMenuResponse.class,
			description = "주문 메뉴 추가 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.RECEIPT_ACCESS_DENIED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_STATUS_NOT_RECEIVED),

		}
	)
	ResponseEntity<ResponseBody<OrderMenuResponse>> postOrderMenu(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long orderId,
		@RequestBody @Valid @NotNull PostOrderMenuRequest postOrderMenuRequest);

	@Operation(
		summary = "주문 메뉴 삭제 API",
		description = "주문 메뉴를 삭제합니다."
			+ " \n 주문 상태가 RECEIVED 상태여야 합니다."
	)
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			description = "주문 메뉴 삭제 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_ACCESS_DENIED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_STATUS_NOT_RECEIVED),
		}
	)
	ResponseEntity<ResponseBody<Void>> deleteOrderMenu(
		@Parameter(hidden = true) UserPassport userPassport,
		@PathVariable Long orderMenuId);

	@Operation(
		summary = "주문 메뉴 수량 변경 API",
		description = "주문 메뉴 수량을 변경합니다."
			+ " \n 주문 상태가 RECEIVED 상태여야 합니다."
			+ " \n 주문 메뉴 상태가 COOKING, COMPLETED 상태여야 합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = OrderMenuResponse.class)))
	@ApiResponseExplanations(
		success = @ApiSuccessResponseExplanation(
			responseClass = OrderMenuResponse.class,
			description = "주문 메뉴 수량 변경 성공"
		),
		errors = {
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_NOT_FOUND),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_STATUS_NOT_RECEIVED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_STATUS_NOT_ALLOWED),
			@ApiErrorResponseExplanation(errorCode = ErrorCode.ORDER_MENU_ACCESS_DENIED),
		}
	)
	ResponseEntity<ResponseBody<OrderMenuResponse>> patchOrderMenuQuantity(
		UserPassport userPassport,
		@PathVariable Long orderMenuId,
		@RequestParam @NotNull @Min(value = 1, message = "수정 개수는 최소 1 이상입니다.") Integer patchQuantity);
}
