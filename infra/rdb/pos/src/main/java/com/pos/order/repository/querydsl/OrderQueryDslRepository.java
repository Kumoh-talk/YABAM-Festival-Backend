package com.pos.order.repository.querydsl;

import java.util.List;
import java.util.Optional;

import com.pos.order.entity.OrderEntity;

import domain.pos.order.entity.vo.OrderStatus;

public interface OrderQueryDslRepository {
	Optional<OrderEntity> findByIdWithMenus(Long orderId);

	Optional<OrderEntity> findByIdWithStore(Long orderId);

	List<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses);

	List<OrderEntity> findReceiptOrdersWithMenu(Long receiptId);

	boolean existsOrderByReceiptId(Long receiptId);

	void updateOrderStatus(Long orderId, OrderStatus orderStatus);

	void subtractOrderPrice(Long orderId, Integer menuPrice);

	void plusOrderPrice(Long orderId, Integer menuPrice);

}
