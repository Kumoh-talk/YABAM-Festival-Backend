package com.pos.table.repository.querydsl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.pos.table.entity.QTableEntity;
import com.pos.table.entity.TableEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TableDslRepositoryImpl implements TableDslRepository {
	private final JPAQueryFactory queryFactory;
	private final QTableEntity qTableEntity = QTableEntity.tableEntity;

	@Override
	public List<TableEntity> findTablesByStoreWithLock(Store store) {
		return queryFactory
			.selectFrom(qTableEntity)
			.where(qTableEntity.store.id.eq(store.getStoreId())
				.and(qTableEntity.deletedAt.isNull()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetch();
	}

	@Override
	public void softDeleteTablesGTQueryTableCount(Store store, Integer queryUpdateTableNumber) {
		queryFactory
			.update(qTableEntity)
			.where(qTableEntity.deletedAt.isNull()
				.and(qTableEntity.store.id.eq(store.getStoreId()))
				.and(qTableEntity.tableNumber.tableNumber.gt(queryUpdateTableNumber)))
			.set(qTableEntity.deletedAt, LocalDateTime.now())
			.execute();
	}

	@Override
	public void updateTableActiveStatusById(boolean isActive, Table savedInActiveTable) {
		queryFactory
			.update(qTableEntity)
			.where(qTableEntity.id.eq(savedInActiveTable.getTableId()))
			.set(qTableEntity.isActive, isActive)
			.execute();
	}

	@Override
	public TableEntity findByIdAndStoreIdForUpdate(Long queryTableId, Long storeId) {
		return queryFactory
			.selectFrom(qTableEntity)
			.where(qTableEntity.id.eq(queryTableId)
				.and(qTableEntity.store.id.eq(storeId)))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();
	}

	@Override
	public boolean existsTableByStoreIdForUpdate(Store responStore) {
		return queryFactory
			.selectOne()
			.from(qTableEntity)
			.where(qTableEntity.store.id.eq(responStore.getStoreId())
				.and(qTableEntity.deletedAt.isNull()))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE) // TODO : 해당 쿼리는 테이블 전체 락을 유발할 가능성 추후 성능 개선 여지 있음
			.fetchFirst() != null;
	}

	@Override
	public List<TableEntity> findByStoreId(Long storeId) {
		return queryFactory
			.selectFrom(qTableEntity)
			.where(qTableEntity.store.id.eq(storeId)
				.and(qTableEntity.deletedAt.isNull()))
			.fetch();
	}
}
