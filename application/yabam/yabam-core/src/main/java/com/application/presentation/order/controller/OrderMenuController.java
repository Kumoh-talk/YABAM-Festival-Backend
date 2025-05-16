package com.application.presentation.order.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.order.api.OrderMenuApi;
import com.application.presentation.order.dto.request.PostOrderMenuRequest;
import com.application.presentation.order.dto.response.OrderMenuResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;
import domain.pos.order.service.OrderMenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class OrderMenuController implements OrderMenuApi {
	private final OrderMenuService orderMenuService;

	@PatchMapping("/api/v1/order-menus/{orderMenuId}/status")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderMenuResponse>> patchOrderMenuStatus(
		UserPassport userPassport,
		@PathVariable Long orderMenuId,
		@RequestParam @NotNull OrderMenuStatus orderMenuStatus) {
		OrderMenu orderMenu = orderMenuService.patchOrderMenuStatus(orderMenuId, userPassport, orderMenuStatus);
		return ResponseEntity.ok(createSuccessResponse(OrderMenuResponse.from(orderMenu)));
	}

	@PostMapping("/api/v1/orders/{orderId}/order-menus")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderMenuResponse>> postOrderMenu(
		UserPassport userPassport,
		@PathVariable Long orderId,
		@RequestBody @Valid @NotNull PostOrderMenuRequest postOrderMenuRequest) {
		OrderMenu orderMenu = orderMenuService.postOrderMenu(orderId, userPassport, postOrderMenuRequest.menuId(),
			postOrderMenuRequest.menuQuantity());
		return ResponseEntity.ok(createSuccessResponse(OrderMenuResponse.from(orderMenu)));
	}

	@DeleteMapping("/api/v1/order-menus/{orderMenuId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteOrderMenu(
		UserPassport userPassport,
		@PathVariable Long orderMenuId) {
		orderMenuService.deleteOrderMenu(orderMenuId, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@PatchMapping("/api/v1/order-menus/{orderMenuId}/quantity")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderMenuResponse>> patchOrderMenuQuantity(
		UserPassport userPassport,
		@PathVariable Long orderMenuId,
		@RequestParam @NotNull @Min(value = 1, message = "수정 개수는 최소 1 이상입니다.") Integer patchQuantity) {
		OrderMenu orderMenu = orderMenuService.patchOrderMenuQuantity(orderMenuId, userPassport, patchQuantity);
		return ResponseEntity.ok(createSuccessResponse(OrderMenuResponse.from(orderMenu)));
	}
}
