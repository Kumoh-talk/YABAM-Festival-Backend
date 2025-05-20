package com.pos.order.repository.querydsl;

import java.util.Optional;

import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.entity.QOrderEntity;
import com.pos.order.entity.QOrderMenuEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.order.entity.vo.OrderMenuStatus;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderMenuQueryDslRepositoryImpl implements OrderMenuQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QOrderMenuEntity qOrderMenuEntity = QOrderMenuEntity.orderMenuEntity;

	@Override
	public Optional<OrderMenuEntity> findByIdWithOrderAndStoreAndOrderLock(Long orderMenuId) {
		OrderMenuEntity orderMenuEntity = jpaQueryFactory
			.selectFrom(qOrderMenuEntity)
			.join(qOrderMenuEntity.order).fetchJoin()
			.join(qOrderMenuEntity.menu).fetchJoin()
			.join(qOrderMenuEntity.menu.store).fetchJoin()
			.where(qOrderMenuEntity.id.eq(orderMenuId))
			.fetchOne();

		QOrderEntity qOrderEntity = QOrderEntity.orderEntity;
		jpaQueryFactory
			.selectFrom(qOrderEntity)
			.where(qOrderEntity.id.eq(orderMenuEntity.getOrder().getId()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();

		return Optional.ofNullable(orderMenuEntity);
	}

	@Override
	public void updateOrderMenuStatusByOrderId(Long orderId, OrderMenuStatus orderStatus) {
		jpaQueryFactory.update(qOrderMenuEntity)
			.set(qOrderMenuEntity.status, orderStatus)
			.where(qOrderMenuEntity.order.id.eq(orderId)
				.and(qOrderMenuEntity.status.ne(OrderMenuStatus.CANCELED)))
			.execute();
	}

	@Override
	public void updateOrderMenuStatus(Long orderMenuId, OrderMenuStatus orderStatus) {
		jpaQueryFactory.update(qOrderMenuEntity)
			.set(qOrderMenuEntity.status, orderStatus)
			.where(qOrderMenuEntity.id.eq(orderMenuId))
			.execute();
	}

	@Override
	public void updateOrderMenuQuantity(Long orderMenuId, Integer patchQuantity) {
		jpaQueryFactory.update(qOrderMenuEntity)
			.set(qOrderMenuEntity.quantity, patchQuantity)
			.where(qOrderMenuEntity.id.eq(orderMenuId))
			.execute();
	}

	@Override
	public void updateOrderMenuCompletedCount(Long orderMenuId, Integer patchCompletedCount) {
		jpaQueryFactory.update(qOrderMenuEntity)
			.set(qOrderMenuEntity.completedCount, patchCompletedCount)
			.where(qOrderMenuEntity.id.eq(orderMenuId))
			.execute();
	}

	@Override
	public boolean existsCookingMenu(Long orderId) {
		return jpaQueryFactory
			.selectOne()
			.from(qOrderMenuEntity)
			.where(qOrderMenuEntity.order.id.eq(orderId)
				.and(qOrderMenuEntity.status.in(OrderMenuStatus.ORDERED, OrderMenuStatus.COOKING)))
			.fetchFirst() != null;
	}
}
