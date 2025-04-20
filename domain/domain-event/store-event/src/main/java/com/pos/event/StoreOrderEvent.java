package com.pos.event;

import java.time.LocalDateTime;
import java.util.List;

//TODO : 추후 내역 수정
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
			String menuStatus // TODO : 추후 enum으로 변경
		) {
		}
	}
}
