package com.pos.order.mapper;

import com.pos.menu.entity.MenuEntity;
import com.pos.order.entity.OrderEntity;
import com.pos.order.entity.OrderMenuEntity;

import domain.pos.cart.entity.CartMenu;
import domain.pos.menu.entity.Menu;
import domain.pos.menu.entity.MenuInfo;
import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderMenuStatus;

public class OrderMenuMapper {
	public static OrderMenu toOrderMenu(OrderMenuEntity orderMenuEntity, Order order, Menu menu) {
		return OrderMenu.builder()
			.orderMenuId(orderMenuEntity.getId())
			.orderMenuStatus(orderMenuEntity.getStatus())
			.quantity(orderMenuEntity.getQuantity())
			.order(order)
			.menu(menu)
			.build();
	}

	public static OrderMenuEntity toOrderMenuEntity(MenuInfo menuInfo, Integer quantity,
		OrderMenuStatus orderMenuStatus, Order order) {
		return OrderMenuEntity.of(
			quantity,
			orderMenuStatus,
			OrderEntity.from(order.getOrderId()),
			MenuEntity.from(menuInfo.getId()));
	}

	public static OrderMenuEntity toOrderMenuEntity(CartMenu cartMenu, OrderMenuStatus status, OrderEntity orderEntity,
		MenuEntity menuEntity) {
		return OrderMenuEntity.of(
			cartMenu.getQuantity(),
			status,
			orderEntity,
			menuEntity
		);
	}
}
