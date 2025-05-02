package com.pos.order.mapper;

import java.util.List;

import com.pos.order.entity.OrderEntity;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.receipt.entity.Receipt;

public class OrderMapper {
	public static Order toOrder(OrderEntity orderEntity, Receipt receipt, List<OrderMenu> orderMenus) {
		return Order.builder()
			.orderId(orderEntity.getId())
			.orderStatus(orderEntity.getStatus())
			.totalPrice(orderEntity.getTotalPrice())
			.receipt(receipt)
			.orderMenus(orderMenus)
			.build();
	}
}
