package com.pos.order.repository.querydsl;

import java.util.Optional;

import com.pos.order.entity.OrderMenuEntity;
import com.pos.order.entity.QOrderMenuEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.order.entity.vo.OrderMenuStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderMenuQueryDslRepositoryImpl implements OrderMenuQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QOrderMenuEntity qOrderMenuEntity = QOrderMenuEntity.orderMenuEntity;

	@Override
	public Optional<OrderMenuEntity> findByIdWithOrderAndStore(Long orderMenuId) {
		OrderMenuEntity orderMenuEntity = jpaQueryFactory
			.selectFrom(qOrderMenuEntity)
			.join(qOrderMenuEntity.order).fetchJoin()
			.join(qOrderMenuEntity.menu).fetchJoin()
			.join(qOrderMenuEntity.menu.store).fetchJoin()
			.where(qOrderMenuEntity.id.eq(orderMenuId))
			.fetchOne();
		return Optional.ofNullable(orderMenuEntity);
	}

	@Override
	public void updateOrderMenuStatus(Long orderId, OrderMenuStatus orderStatus) {
		jpaQueryFactory.update(qOrderMenuEntity)
			.set(qOrderMenuEntity.status, orderStatus)
			.where(qOrderMenuEntity.order.id.eq(orderId))
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
