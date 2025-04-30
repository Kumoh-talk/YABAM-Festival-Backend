package com.pos.producer.store.order.common;

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
					.menuStatus(
						orderMenu.getOrder().getOrderStatus().name()) // TODO : 해당 상태값을 Order의 상태가 아닌 Order로 수정해야함
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
					// TODO : order.getCreatedAt() 추가되면 여기에 추가해야함
					.build()
			)
			.build();
	}

}
