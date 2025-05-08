package com.application.presentation.order.dto.response;

import java.util.List;

import com.application.presentation.receipt.dto.response.ReceiptResponse;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 응답 DTO")
public record OrderResponse(
	@Schema(description = "주문 id", example = "1")
	Long orderId,
	@Schema(description = "주문 상태", example = "RECEIVED")
	OrderStatus orderStatus,
	@Schema(description = "주문 총 가격", example = "20000")
	Integer totalPrice,
	@Schema(description = "영수증 정보")
	ReceiptResponse receipt,
	@Schema(description = "주문 메뉴 리스트")
	List<OrderMenuResponse> orderMenus
) {

	public static OrderResponse from(Order order) {
		return new OrderResponse(
			order.getOrderId(),
			order.getOrderStatus(),
			order.getTotalPrice(),
			ReceiptResponse.from(order.getReceipt()),
			order.getOrderMenus().stream()
				.map(OrderMenuResponse::from)
				.toList()
		);
	}

}
