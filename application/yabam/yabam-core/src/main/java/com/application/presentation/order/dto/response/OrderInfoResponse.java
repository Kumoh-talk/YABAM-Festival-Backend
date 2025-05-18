package com.application.presentation.order.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	Integer totalPrice,

	@Schema(description = "주문 시간", example = "2023-10-01T10:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createdAt
) {
	public static OrderInfoResponse from(Order order) {
		return new OrderInfoResponse(
			order.getOrderId(),
			order.getOrderStatus(),
			order.getTotalPrice(),
			order.getCreatedAt()
		);
	}
}
