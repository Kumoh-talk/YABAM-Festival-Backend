package com.pos.order.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pos.order.entity.OrderEntity;
import com.pos.order.entity.QOrderEntity;
import com.pos.order.entity.QOrderMenuEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import domain.pos.order.entity.vo.OrderStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderQueryDslRepositoryImpl implements OrderQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QOrderEntity qOrderEntity = QOrderEntity.orderEntity;

	@Override
	public Optional<OrderEntity> findByIdWithMenus(Long orderId) {
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;

		OrderEntity orderEntity = jpaQueryFactory
			.selectFrom(qOrderEntity).distinct()
			.join(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
			.join(qOrderMenu.menu).fetchJoin()
			.where(qOrderEntity.id.eq(orderId))
			.fetchOne();

		return Optional.ofNullable(orderEntity);
	}

	@Override
	public Optional<OrderEntity> findByIdWithStore(Long orderId) {
		OrderEntity orderEntity = jpaQueryFactory
			.selectFrom(qOrderEntity)
			.join(qOrderEntity.receipt).fetchJoin()
			.join(qOrderEntity.receipt.sale).fetchJoin()
			.join(qOrderEntity.receipt.sale.store).fetchJoin()
			.where(qOrderEntity.id.eq(orderId))
			.fetchOne();

		return Optional.ofNullable(orderEntity);
	}

	@Override
	public List<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses) {
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;

		return jpaQueryFactory
			.selectFrom(qOrderEntity).distinct()
			.join(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
			.join(qOrderMenu.menu).fetchJoin()
			.join(qOrderEntity.receipt).fetchJoin()
			.join(qOrderEntity.receipt.table).fetchJoin()
			.where(qOrderEntity.receipt.sale.id.eq(saleId)
				.and(qOrderEntity.status.in(orderStatuses)))
			.orderBy(qOrderEntity.createdAt.asc(), qOrderEntity.id.asc())
			.fetch();
	}

	@Override
	public List<OrderEntity> findReceiptOrdersWithMenu(UUID receiptId) {
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;

		return jpaQueryFactory
			.selectFrom(qOrderEntity).distinct()
			.join(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
			.join(qOrderMenu.menu).fetchJoin()
			.where(qOrderEntity.receipt.id.eq(receiptId))
			.orderBy(qOrderEntity.createdAt.asc(), qOrderEntity.id.asc())
			.fetch();
	}

	@Override
	public boolean existsOrderByReceiptId(UUID receiptId) {
		return jpaQueryFactory
			.selectOne()
			.from(qOrderEntity)
			.where(qOrderEntity.receipt.id.eq(receiptId))
			.fetchFirst() != null;
	}

	@Override
	public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
		JPAUpdateClause updateClause = jpaQueryFactory
			.update(qOrderEntity)
			.where(qOrderEntity.id.eq(orderId));

		if (orderStatus == OrderStatus.CANCELED) {
			updateClause.set(qOrderEntity.status, orderStatus)
				.set(qOrderEntity.totalPrice, 0);
		} else {
			updateClause.set(qOrderEntity.status, orderStatus);
		}

		updateClause.execute();
	}

	@Override
	public void subtractOrderPrice(Long orderId, Integer menuPrice) {
		jpaQueryFactory.update(qOrderEntity)
			.set(qOrderEntity.totalPrice, qOrderEntity.totalPrice.subtract(menuPrice))
			.where(qOrderEntity.id.eq(orderId))
			.execute();
	}

	@Override
	public void plusOrderPrice(Long orderId, Integer menuPrice) {
		jpaQueryFactory.update(qOrderEntity)
			.set(qOrderEntity.totalPrice, qOrderEntity.totalPrice.add(menuPrice))
			.where(qOrderEntity.id.eq(orderId))
			.execute();
	}
}
