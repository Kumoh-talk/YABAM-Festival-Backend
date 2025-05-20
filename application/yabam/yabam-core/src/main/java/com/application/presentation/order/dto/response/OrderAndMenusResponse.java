package com.application.presentation.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 및 메뉴 리스트 응답 DTO")
public record OrderAndMenusResponse(
	@Schema(description = "주문 id", example = "1")
	Long orderId,
	@Schema(description = "주문 상태", example = "RECEIVED")
	OrderStatus orderStatus,
	@Schema(description = "주문 총 가격", example = "20000")
	Integer totalPrice,
	@Schema(description = "주문 설명", example = "서비스")
	String description,
	@Schema(description = "주문 시간", example = "2023-10-01T10:00:00")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	LocalDateTime createdAt,
	@Schema(description = "주문 메뉴 리스트")
	List<OrderMenuResponse> orderMenus
) {
	public static OrderAndMenusResponse from(Order order) {
		return new OrderAndMenusResponse(
			order.getOrderId(),
			order.getOrderStatus(),
			order.getTotalPrice(),
			order.getDescription(),
			order.getCreatedAt(),
			order.getOrderMenus().stream()
				.map(OrderMenuResponse::from)
				.toList()
		);
	}
}
