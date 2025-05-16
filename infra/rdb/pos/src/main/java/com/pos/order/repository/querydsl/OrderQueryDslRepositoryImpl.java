package com.pos.order.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.pos.order.entity.OrderEntity;
import com.pos.order.entity.QOrderEntity;
import com.pos.order.entity.QOrderMenuEntity;
import com.pos.sale.entity.QSaleEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import domain.pos.order.entity.vo.OrderStatus;
import jakarta.persistence.LockModeType;
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
		QSaleEntity qSaleEntity = QSaleEntity.saleEntity;

		OrderEntity orderEntity = jpaQueryFactory
			.selectFrom(qOrderEntity)
			.join(qOrderEntity.receipt).fetchJoin()
			.join(qOrderEntity.receipt.sale, qSaleEntity).fetchJoin()
			.join(qSaleEntity.store).fetchJoin()
			.where(qOrderEntity.id.eq(orderId))
			.fetchOne();

		return Optional.ofNullable(orderEntity);
	}

	@Override
	public Optional<OrderEntity> findByIdWithStoreAndMenusAndLock(Long orderId) {
		QSaleEntity qSaleEntity = QSaleEntity.saleEntity;

		OrderEntity orderEntity = jpaQueryFactory
			.selectFrom(qOrderEntity).distinct()
			.join(qOrderEntity.receipt).fetchJoin()
			.join(qOrderEntity.receipt.sale, qSaleEntity).fetchJoin()
			.join(qSaleEntity.store).fetchJoin()
			.join(qOrderEntity.orderMenus).fetchJoin()
			.where(qOrderEntity.id.eq(orderId))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();

		return Optional.ofNullable(orderEntity);
	}

	@Override
	public Slice<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses, int pageSize,
		Long lastOrderId) {
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;

		List<OrderEntity> orders = jpaQueryFactory
			.selectFrom(qOrderEntity).distinct()
			.join(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
			.join(qOrderMenu.menu).fetchJoin()
			.join(qOrderEntity.receipt).fetchJoin()
			.join(qOrderEntity.receipt.table).fetchJoin()
			.where(qOrderEntity.receipt.sale.id.eq(saleId)
				.and(qOrderEntity.status.in(orderStatuses))
				.and(lastOrderId == null ? Expressions.TRUE : qOrderEntity.id.lt(lastOrderId)))
			.orderBy(qOrderEntity.id.desc())
			.limit(pageSize + 1)
			.fetch();

		boolean hasNext = orders.size() > pageSize;
		if (hasNext) {
			orders.remove(orders.size() - 1);
		}

		return new SliceImpl<>(orders, Pageable.ofSize(pageSize), hasNext);
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
