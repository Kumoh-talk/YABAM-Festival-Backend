package com.pos.producer.mapper;

import java.util.List;

import com.pos.event.StoreOrderEvent;

import domain.pos.order.entity.Order;
import domain.pos.table.entity.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StoreOrderEventMapper {
	public static StoreOrderEvent toStoreOrderEvent(Table table, Order order) {
		List<StoreOrderEvent.OrderDto.OrderMenuDto> orderMenus = order.getOrderMenus().stream()
			.map(orderMenu -> {
				return StoreOrderEvent.OrderDto.OrderMenuDto.builder()
					.menuId(orderMenu.getOrderMenuId())
					.menuName(orderMenu.getMenu().getMenuInfo().getName())
					.menuPrice(orderMenu.getMenu().getMenuInfo().getPrice())
					.menuCount(orderMenu.getQuantity())
					.build();
			})
			.toList();
		return StoreOrderEvent.builder()
			.tableId(table.getTableId())
			.tableNumber(table.getTableNumber())
			.orderDto(
				StoreOrderEvent.OrderDto.builder()
					.orderId(order.getOrderId())
					.orderMenuDtos(orderMenus)
					.orderStatus(order.getOrderStatus().name())
					// TODO : order.getCreatedAt() 추가되면 여기에 추가해야함
					.build()
			)
			.build();
	}
}
