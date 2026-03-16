package com.pos.order.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.pos.order.entity.OrderEntity;

import domain.pos.order.entity.Order;
import domain.pos.order.entity.vo.OrderStatus;

public interface OrderQueryDslRepository {
	Optional<OrderEntity> findByIdWithMenus(Long orderId);

	Optional<OrderEntity> findByIdWithStore(Long orderId);

	Optional<OrderEntity> findByIdWithStoreAndMenusAndLock(Long orderId);

	Slice<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses, int pageSize,
		Long lastOrderId);

	List<OrderEntity> findReceiptOrdersWithMenu(UUID receiptId);

	boolean existsOrderByReceiptId(UUID receiptId);

	void updateOrderStatus(Long orderId, OrderStatus orderStatus);

	void updateCustomOrder(Long orderId, Order patchOrder);

	void subtractOrderPrice(Long orderId, Integer menuPrice);

	void plusOrderPrice(Long orderId, Integer menuPrice);

}
