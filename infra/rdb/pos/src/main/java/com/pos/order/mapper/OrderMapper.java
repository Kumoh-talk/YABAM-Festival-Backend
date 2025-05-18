package com.pos.order.mapper;

import java.util.List;

import com.pos.order.entity.OrderEntity;
import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.OrderMenu;
import domain.pos.order.entity.vo.OrderStatus;
import domain.pos.receipt.entity.Receipt;

public class OrderMapper {
	public static OrderEntity toOrderEntity(OrderStatus orderStatus, Integer totalPrice, Receipt receipt) {
		return OrderEntity.builder()
			.status(orderStatus)
			.totalPrice(totalPrice)
			.receipt(ReceiptEntity.from(receipt.getReceiptInfo().getReceiptId()))
			.build();
	}

	public static Order toOrder(OrderEntity orderEntity, Receipt receipt, List<OrderMenu> orderMenus) {
		return Order.builder()
			.orderId(orderEntity.getId())
			.orderStatus(orderEntity.getStatus())
			.totalPrice(orderEntity.getTotalPrice())
			.createdAt(orderEntity.getCreatedAt())
			.receipt(receipt)
			.orderMenus(orderMenus)
			.build();
	}
}
