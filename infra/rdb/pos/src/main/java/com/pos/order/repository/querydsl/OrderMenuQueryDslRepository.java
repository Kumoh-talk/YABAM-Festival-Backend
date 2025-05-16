package com.pos.order.repository.querydsl;

import java.util.Optional;

import com.pos.order.entity.OrderMenuEntity;

import domain.pos.order.entity.vo.OrderMenuStatus;

public interface OrderMenuQueryDslRepository {
	Optional<OrderMenuEntity> findByIdWithOrderAndStoreAndOrderLock(Long orderMenuId);

	void updateOrderMenuStatus(Long orderId, OrderMenuStatus orderStatus);

	void updateOrderMenuQuantity(Long orderMenuId, Integer patchQuantity);

	boolean existsCookingMenu(Long orderId);
}
