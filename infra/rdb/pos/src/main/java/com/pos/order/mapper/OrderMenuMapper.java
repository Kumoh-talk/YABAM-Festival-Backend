package com.pos.order.mapper;

import com.pos.order.entity.OrderMenuEntity;

import domain.pos.menu.entity.Menu;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;

public class OrderMenuMapper {

	public static OrderMenu toOrderMenu(OrderMenuEntity orderMenuEntity, Order order, Menu menu) {
		return OrderMenu.builder()
			.orderMenuId(orderMenuEntity.getId())
			.quantity(orderMenuEntity.getQuantity())
			.order(order)
			.menu(menu)
			.build();
	}

	public static OrderMenuEntity toOrderMenuEntity(OrderMenu orderMenu) {
		return OrderMenuEntity.of(orderMenu);
	}
}
