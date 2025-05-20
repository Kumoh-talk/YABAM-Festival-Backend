package com.application.presentation.order.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.application.global.response.GlobalSliceResponse;
import com.application.presentation.order.api.OrderApi;
import com.application.presentation.order.dto.request.PostCustomOrderRequest;
import com.application.presentation.order.dto.request.PostOrderMenuRequest;
import com.application.presentation.order.dto.response.OrderAndMenusResponse;
import com.application.presentation.order.dto.response.OrderInfoResponse;
import com.application.presentation.order.dto.response.OrderResponse;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.order.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Validated
public class OrderController implements OrderApi {
	private final OrderService orderService;

	@PostMapping("/api/v1/receipts/{receiptId}/orders/with-cart")
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderResponse>> postOrderWithCart(
		UserPassport userPassport,
		@PathVariable UUID receiptId) {
		Order order = orderService.postOrderWithCart(receiptId, userPassport);
		return ResponseEntity.ok(createSuccessResponse(OrderResponse.from(order)));
	}

	@PostMapping("/api/v1/receipts/{receiptId}/orders/direct")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderResponse>> postOrderWithoutCart(
		UserPassport userPassport,
		@PathVariable UUID receiptId, @RequestBody @Valid @NotEmpty List<PostOrderMenuRequest> postOrderMenuRequests) {
		Order order = orderService.postOrderWithoutCart(receiptId, userPassport, postOrderMenuRequests.stream()
			.map(PostOrderMenuRequest::toOrderMenu).toList());
		return ResponseEntity.ok(createSuccessResponse(OrderResponse.from(order)));
	}

	@PostMapping("/api/v1/receipts/{receiptId}/orders/custom")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderAndMenusResponse>> postCustomOrder(
		UserPassport userPassport,
		@PathVariable UUID receiptId, @RequestBody @Valid PostCustomOrderRequest postCustomOrderRequest) {
		Order order = orderService.postCustomOrder(receiptId, userPassport, postCustomOrderRequest.toOrder());
		return ResponseEntity.ok(createSuccessResponse(OrderAndMenusResponse.from(order)));
	}

	@PatchMapping("/api/v1/orders/custom/{orderId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderAndMenusResponse>> patchCustomOrder(
		UserPassport userPassport,
		@PathVariable Long orderId, @RequestBody @Valid PostCustomOrderRequest patchCustomOrderRequest) {
		Order order = orderService.patchCustomOrder(orderId, userPassport, patchCustomOrderRequest.toOrder());
		return ResponseEntity.ok(createSuccessResponse(OrderAndMenusResponse.from(order)));
	}

	@DeleteMapping("/api/v1/orders/{orderId}")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<Void>> deleteOrder(
		UserPassport userPassport, @PathVariable Long orderId) {
		orderService.deleteOrder(orderId, userPassport);
		return ResponseEntity.ok(createSuccessResponse());
	}

	@PatchMapping("/api/v1/orders/{orderId}/status")
	@AssignUserPassport
	public ResponseEntity<ResponseBody<OrderInfoResponse>> patchOrderStatus(
		UserPassport userPassport,
		@PathVariable Long orderId,
		@RequestParam @NotNull OrderStatus orderStatus) {
		Order order = orderService.patchOrderStatus(orderId, userPassport, orderStatus);
		return ResponseEntity.ok(createSuccessResponse(OrderInfoResponse.from(order)));
	}

	@GetMapping("/api/v1/receipts/{receiptId}/orders")
	public ResponseEntity<ResponseBody<List<OrderAndMenusResponse>>> getReceiptOrders(@PathVariable UUID receiptId) {
		List<OrderAndMenusResponse> orderAndMenusResponse = orderService.getReceiptOrders(receiptId).stream()
			.map(OrderAndMenusResponse::from)
			.toList();
		return ResponseEntity.ok(createSuccessResponse(orderAndMenusResponse));
	}

	@GetMapping("/api/v1/sales/{saleId}/orders")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<GlobalSliceResponse<OrderResponse>>> getSaleOrderSlice(
		UserPassport userPassport,
		@PathVariable Long saleId,
		@RequestParam @NotEmpty List<OrderStatus> orderStatuses,
		@RequestParam @Min(value = 1, message = "페이지 크기는 최소 1 이상입니다.") int pageSize,
		@RequestParam(required = false) Long lastOrderId) {
		Slice<OrderResponse> orderResponses = orderService.getSaleOrderSlice(userPassport, saleId, orderStatuses,
			pageSize, lastOrderId).map(OrderResponse::from);
		return ResponseEntity.ok(createSuccessResponse(GlobalSliceResponse.from(orderResponses)));
	}

	@GetMapping("/api/v1/orders/{orderId}")
	public ResponseEntity<ResponseBody<OrderAndMenusResponse>> getOrder(@PathVariable Long orderId) {
		Order order = orderService.getOrder(orderId);
		return ResponseEntity.ok(createSuccessResponse(OrderAndMenusResponse.from(order)));
	}
}
