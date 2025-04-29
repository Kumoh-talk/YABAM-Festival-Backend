package com.pos.event;

import java.time.LocalDateTime;
import java.util.List;

public record StoreOrderEvent(
	Long tableId,
	Integer tableNumber,
	Order order
) {
	public record Order(
		Long orderId,
		LocalDateTime createdAt,
		List<OrderMenu> orderMenu
	) {
		public record OrderMenu(
			Long menuId,
			String menuName,
			Integer menuPrice,
			Integer menuCount,
			String menuStatus
		) {
		}
	}
}
