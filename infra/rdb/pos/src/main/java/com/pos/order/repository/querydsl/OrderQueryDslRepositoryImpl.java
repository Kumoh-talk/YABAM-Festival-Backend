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
import com.pos.receipt.entity.QReceiptEntity;
import com.pos.sale.entity.QSaleEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

import domain.pos.order.entity.Order;
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
				.leftJoin(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
				.leftJoin(qOrderMenu.menu).fetchJoin()
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
				.leftJoin(qOrderEntity.orderMenus).fetchJoin()
				.where(qOrderEntity.id.eq(orderId))
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.fetchOne();

		return Optional.ofNullable(orderEntity);
	}

	@Override
	public Slice<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses, int pageSize,
			Long lastOrderId) {
		QReceiptEntity qReceipt = QReceiptEntity.receiptEntity;

		// 1단계: sale_id 기반 receipt_ids 조회 (idx_receipts_sale_id 활용)
		List<UUID> receiptIds = jpaQueryFactory
				.select(qReceipt.id)
				.from(qReceipt)
				.where(qReceipt.sale.id.eq(saleId))
				.fetch();

		if (receiptIds.isEmpty()) {
			return new SliceImpl<>(List.of(), Pageable.ofSize(pageSize), false);
		}

		// 2단계: JOIN 없이 order_ids만 조회 (PRIMARY key Backward scan → LIMIT 조기 종료)
		List<Long> orderIds = jpaQueryFactory
				.select(qOrderEntity.id)
				.from(qOrderEntity)
				.where(qOrderEntity.receipt.id.in(receiptIds)
						.and(qOrderEntity.status.in(orderStatuses))
						.and(lastOrderId == null ? Expressions.TRUE : qOrderEntity.id.lt(lastOrderId)))
				.orderBy(qOrderEntity.id.desc())
				.limit(pageSize + 1)
				.fetch();

		if (orderIds.isEmpty()) {
			return new SliceImpl<>(List.of(), Pageable.ofSize(pageSize), false);
		}

		boolean hasNext = orderIds.size() > pageSize;
		List<Long> fetchIds = hasNext ? orderIds.subList(0, pageSize) : orderIds;

		// 3단계: fetchIds로 receipt, table, menus 한번에 조회 (N+1 방지)
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;
		List<OrderEntity> orders = jpaQueryFactory
				.selectFrom(qOrderEntity).distinct()
				.join(qOrderEntity.receipt).fetchJoin()
				.join(qOrderEntity.receipt.table).fetchJoin()
				.leftJoin(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
				.leftJoin(qOrderMenu.menu).fetchJoin()
				.where(qOrderEntity.id.in(fetchIds))
				.orderBy(qOrderEntity.id.desc())
				.fetch();

		return new SliceImpl<>(orders, Pageable.ofSize(pageSize), hasNext);
	}

	@Override
	public List<OrderEntity> findSaleOrdersWithMenuAndTable(Long saleId, List<OrderStatus> orderStatuses) {
		QReceiptEntity qReceipt = QReceiptEntity.receiptEntity;

		// 1단계: sale_id 기반 receipt_ids 조회 (idx_receipts_sale_id 활용)
		List<UUID> receiptIds = jpaQueryFactory
				.select(qReceipt.id)
				.from(qReceipt)
				.where(qReceipt.sale.id.eq(saleId))
				.fetch();

		if (receiptIds.isEmpty()) {
			return List.of();
		}

		// 2단계: JOIN 없이 order_ids 조회 (인덱스 활용)
		List<Long> orderIds = jpaQueryFactory
				.select(qOrderEntity.id)
				.from(qOrderEntity)
				.where(qOrderEntity.receipt.id.in(receiptIds)
						.and(qOrderEntity.status.in(orderStatuses)))
				.orderBy(qOrderEntity.id.desc())
				.fetch();

		if (orderIds.isEmpty()) {
			return List.of();
		}

		// 3단계: fetchIds로 receipt, table, menus 한번에 조회 (N+1 방지)
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;
		return jpaQueryFactory
				.selectFrom(qOrderEntity).distinct()
				.join(qOrderEntity.receipt).fetchJoin()
				.join(qOrderEntity.receipt.table).fetchJoin()
				.leftJoin(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
				.leftJoin(qOrderMenu.menu).fetchJoin()
				.where(qOrderEntity.id.in(orderIds))
				.orderBy(qOrderEntity.id.desc())
				.fetch();
	}

	@Override
	public List<OrderEntity> findReceiptOrdersWithMenu(UUID receiptId) {
		QOrderMenuEntity qOrderMenu = QOrderMenuEntity.orderMenuEntity;

		return jpaQueryFactory
				.selectFrom(qOrderEntity).distinct()
				.leftJoin(qOrderEntity.orderMenus, qOrderMenu).fetchJoin()
				.leftJoin(qOrderMenu.menu).fetchJoin()
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
	public void updateCustomOrder(Long orderId, Order patchOrder) {
		jpaQueryFactory.update(qOrderEntity)
				.set(qOrderEntity.totalPrice, patchOrder.getTotalPrice())
				.set(qOrderEntity.description, patchOrder.getDescription())
				.where(qOrderEntity.id.eq(orderId))
				.execute();
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
