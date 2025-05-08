package com.pos.receipt.repository.querydsl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.pos.receipt.entity.QReceiptCustomerEntity;
import com.pos.receipt.entity.QReceiptEntity;
import com.pos.receipt.entity.ReceiptEntity;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.receipt.entity.Receipt;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReceiptQueryDslRepositoryImpl implements ReceiptQueryDslRepository {
	private final JPAQueryFactory jpaQueryFactory;
	private final QReceiptEntity qReceiptEntity = QReceiptEntity.receiptEntity;

	@Override
	public Optional<ReceiptEntity> findByIdWithTableAndStore(Long receiptId) {
		ReceiptEntity receiptEntity = jpaQueryFactory.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.table).fetchJoin()
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.where(qReceiptEntity.id.eq(receiptId))
			.fetchOne();

		return Optional.ofNullable(receiptEntity);
	}

	@Override
	public List<ReceiptEntity> findStopReceiptsByIdWithTableAndStore(List<Long> receiptIds) {
		return jpaQueryFactory.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.table).fetchJoin()
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.where(qReceiptEntity.id.in(receiptIds)
				.and(qReceiptEntity.stopUsageTime.isNotNull())
				.and(qReceiptEntity.isAdjustment.isFalse()))
			.orderBy(qReceiptEntity.id.asc())
			.fetch();
	}

	@Override
	public List<ReceiptEntity> findStopReceiptsByIdWithStore(List<Long> receiptIds) {
		return jpaQueryFactory.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.where(qReceiptEntity.id.in(receiptIds)
				.and(qReceiptEntity.stopUsageTime.isNotNull())
				.and(qReceiptEntity.isAdjustment.isFalse()))
			.orderBy(qReceiptEntity.id.asc())
			.fetch();
	}

	@Override
	public Optional<ReceiptEntity> findNonStopReceiptsByIdWithTableAndStoreAndLock(Long receiptId) {
		ReceiptEntity receiptEntity = jpaQueryFactory.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.table).fetchJoin()
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.where(qReceiptEntity.id.eq(receiptId)
				.and(qReceiptEntity.stopUsageTime.isNull()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();

		return Optional.ofNullable(receiptEntity);
	}

	@Override
	public List<ReceiptEntity> findNonStopReceiptsByIdWithStoreAndLock(List<Long> receiptIds) {
		return jpaQueryFactory.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.where(qReceiptEntity.id.in(receiptIds)
				.and(qReceiptEntity.stopUsageTime.isNull()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.orderBy(qReceiptEntity.id.asc())
			.fetch();
	}

	@Override
	public Page<ReceiptEntity> findAdjustedReceiptPageBySaleId(Pageable pageable, Long saleId) {
		List<ReceiptEntity> receipts = jpaQueryFactory
			.selectFrom(qReceiptEntity)
			.where(qReceiptEntity.sale.id.eq(saleId)
				.and(qReceiptEntity.isAdjustment.isTrue()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(qReceiptEntity.createdAt.desc(), qReceiptEntity.id.desc())
			.fetch();

		Long total = jpaQueryFactory
			.select(qReceiptEntity.count())
			.from(qReceiptEntity)
			.where(qReceiptEntity.sale.id.eq(saleId)
				.and(qReceiptEntity.isAdjustment.isTrue()))
			.fetchOne();

		return new PageImpl<>(receipts, pageable, total != null ? total : 0L);
	}

	@Override
	public ReceiptEntity findNonAdjustReceipt(Long tableId) {
		return jpaQueryFactory
			.selectFrom(qReceiptEntity)
			.where(
				qReceiptEntity.table.id.eq(tableId),
				qReceiptEntity.isAdjustment.isFalse()
			)
			.fetchOne();
	}

	@Override
	public Slice<ReceiptEntity> findCustomerReceiptSliceWithStore(int pageSize, Long lastReceiptId,
		Long customerId) {
		QReceiptCustomerEntity qReceiptCustomerEntity = QReceiptCustomerEntity.receiptCustomerEntity;

		List<ReceiptEntity> receipts = jpaQueryFactory
			.selectFrom(qReceiptEntity)
			.join(qReceiptEntity.sale).fetchJoin()
			.join(qReceiptEntity.sale.store).fetchJoin()
			.join(qReceiptCustomerEntity).on(qReceiptCustomerEntity.receipt.id.eq(qReceiptEntity.id))
			.where(qReceiptCustomerEntity.customerId.eq(customerId)
				.and(lastReceiptId == null ? Expressions.TRUE : qReceiptEntity.id.lt(lastReceiptId))
				.and(qReceiptEntity.isAdjustment.isTrue()))
			.orderBy(qReceiptEntity.id.desc())
			.limit(pageSize + 1)
			.fetch();

		boolean hasNext = receipts.size() > pageSize;
		if (hasNext) {
			receipts.remove(receipts.size() - 1);
		}

		return new SliceImpl<>(receipts, Pageable.ofSize(pageSize), hasNext);
	}

	@Override
	public Optional<ReceiptEntity> findByIdWithOrders(Long receiptId) {
		ReceiptEntity receiptEntity = jpaQueryFactory
			.selectFrom(qReceiptEntity).distinct()
			.join(qReceiptEntity.orders).fetchJoin()
			.where(qReceiptEntity.id.eq(receiptId))
			.fetchOne();

		return Optional.ofNullable(receiptEntity);
	}

	@Override
	public void restartReceipts(List<Receipt> receipts) {
		jpaQueryFactory.update(qReceiptEntity)
			.set(qReceiptEntity.stopUsageTime, (LocalDateTime)null)
			.set(qReceiptEntity.isAdjustment, false)
			.set(qReceiptEntity.occupancyFee, (Integer)null)
			.where(qReceiptEntity.id.in(
				receipts.stream().map(receipt -> receipt.getReceiptInfo().getReceiptId()).toList()))
			.execute();
	}

	@Override
	public void adjustReceipts(List<Receipt> receipts) {
		jpaQueryFactory.update(qReceiptEntity)
			.set(qReceiptEntity.isAdjustment, true)
			.where(qReceiptEntity.id.in(
				receipts.stream().map(receipt -> receipt.getReceiptInfo().getReceiptId()).toList()))
			.execute();
	}

	@Override
	public void startReceiptUsage(Long receiptId) {
		jpaQueryFactory.update(qReceiptEntity)
			.set(qReceiptEntity.startUsageTime, LocalDateTime.now())
			.where(qReceiptEntity.id.eq(receiptId)
				.and(qReceiptEntity.startUsageTime.isNull()))
			.execute();
	}
}
