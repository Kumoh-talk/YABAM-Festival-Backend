package com.application.presentation.order.dto.response;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 세부정보 응답 DTO")
public record OrderInfoResponse(
	@Schema(description = "주문 id", example = "1")
	Long orderId,
	@Schema(description = "주문 상태", example = "RECEIVED")
	OrderStatus orderStatus,
	@Schema(description = "주문 총 가격", example = "20000")
	Integer totalPrice
) {
	public static OrderInfoResponse from(Order order) {
		return new OrderInfoResponse(
			order.getOrderId(),
			order.getOrderStatus(),
			order.getTotalPrice()
		);
	}
}
