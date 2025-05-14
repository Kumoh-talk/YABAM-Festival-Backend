package com.pos.call.repository.dsl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.pos.call.entity.CallEntity;
import com.pos.call.entity.QCallEntity;
import com.pos.receipt.entity.QReceiptEntity;
import com.pos.sale.entity.QSaleEntity;
import com.pos.store.entity.QStoreEntity;
import com.pos.table.entity.QTableEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CallDslRepositoryImpl implements CallDslRepository {
	private final JPAQueryFactory queryFactory;
	private final QCallEntity qCallEntity = QCallEntity.callEntity;
	private final QReceiptEntity qReceiptEntity = QReceiptEntity.receiptEntity;
	private final QTableEntity qTableEntity = QTableEntity.tableEntity;
	private final QSaleEntity qSaleEntity = QSaleEntity.saleEntity;
	private final QStoreEntity qStoreEntity = QStoreEntity.storeEntity;

	@Override
	public Slice<CallEntity> getNonCompleteCallsWithReceiptTable(Long saleId, Long lastCallId, int size) {
		List<CallEntity> fetch = queryFactory
			.select(qCallEntity)
			.from(qCallEntity)
			.join(qCallEntity.receipt, qReceiptEntity).fetchJoin()
			.join(qReceiptEntity.table, qTableEntity).fetchJoin()
			.where(cursorWhereCondition(saleId, lastCallId))
			.orderBy(qCallEntity.id.asc())
			.limit(size + 1)
			.fetch();

		boolean hasNext = false;
		if (fetch.size() > size) {
			hasNext = true;
			fetch.remove(size);
		}
		return new SliceImpl<>(fetch, PageRequest.of(0, fetch.size()), hasNext);
	}

	@Override
	public boolean isExistsCallOwner(Long callId, Long userId) {
		return queryFactory
			.select(qCallEntity.id)
			.from(qCallEntity)
			.join(qCallEntity.sale, qSaleEntity)
			.join(qSaleEntity.store, qStoreEntity)
			.where(qCallEntity.id.eq(callId)
				.and(qStoreEntity.ownerId.eq(userId)))
			.fetchFirst() != null;
	}

	private BooleanExpression cursorWhereCondition(Long saleId, Long lastCallId) {
		if (lastCallId == null) {
			return qCallEntity.sale.id.eq(saleId)
				.and(qCallEntity.isCompleted.isFalse());
		}
		return qCallEntity.sale.id.eq(saleId)
			.and(qCallEntity.id.lt(lastCallId))
			.and(qCallEntity.isCompleted.isFalse());
	}
}
