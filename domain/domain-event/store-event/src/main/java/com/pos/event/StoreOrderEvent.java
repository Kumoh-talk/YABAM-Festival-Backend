package com.pos.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record StoreOrderEvent(
	Long tableId,
	Integer tableNumber,
	OrderDto orderDto
) {
	@Builder
	public record OrderDto(
		Long orderId,
		LocalDateTime createdAt,
		List<OrderMenuDto> orderMenuDtos,
		String orderStatus
	) {
		@Builder
		public record OrderMenuDto(
			Long menuId,
			String menuName,
			Integer menuPrice,
			Integer menuCount
			// TODO : OrderMenuStatus 추가되면 수정해야함
		) {
		}
	}
}
