package com.pos.order.repository.querydsl;

import java.util.Optional;

import com.pos.order.entity.OrderMenuEntity;

import domain.pos.order.entity.vo.OrderMenuStatus;

public interface OrderMenuQueryDslRepository {
	Optional<OrderMenuEntity> findByIdWithOrderAndStoreAndOrderLock(Long orderMenuId);

	void updateOrderMenuStatusByOrderId(Long orderId, OrderMenuStatus orderStatus);

	void updateOrderMenuStatus(Long orderMenuId, OrderMenuStatus orderStatus);

	void updateOrderMenuQuantity(Long orderMenuId, Integer patchQuantity);

	void updateOrderMenuCompletedCount(Long orderMenuId, Integer patchCompletedCount);

	boolean existsCookingMenu(Long orderId);
}
