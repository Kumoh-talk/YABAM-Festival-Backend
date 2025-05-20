package com.application.presentation.order.dto.request;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커스텀 메뉴 생성 DTO")
public record PostCustomOrderRequest(
	// 음수 가능
	@Schema(description = "커스텀 주문 가격(음수 가능)", example = "2000")
	Integer totalPrice,

	@Schema(description = "커스텀 주문 설명", example = "서비스")
	String description
) {
	public Order toOrder() {
		return Order.builder()
			.orderStatus(OrderStatus.COMPLETED)
			.totalPrice(totalPrice)
			.description(description)
			.build();
	}
}
