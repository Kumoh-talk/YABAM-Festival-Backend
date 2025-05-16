package com.pos.table.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pos.store.entity.QStoreEntity;
import com.pos.table.entity.QTableEntity;
import com.pos.table.entity.TableEntity;
import com.pos.table.vo.TablePointVo;
import com.querydsl.jpa.impl.JPAQueryFactory;

import domain.pos.table.entity.Table;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TableDslRepositoryImpl implements TableDslRepository {
	private final JPAQueryFactory queryFactory;
	private final QTableEntity qTableEntity = QTableEntity.tableEntity;
	private final QStoreEntity qStoreEntity = QStoreEntity.storeEntity;

	@Override
	public void updateTableActiveStatusById(boolean isActive, Table savedInActiveTable) {
		queryFactory
			.update(qTableEntity)
			.where(qTableEntity.id.eq(savedInActiveTable.getTableId()))
			.set(qTableEntity.isActive, isActive)
			.execute();
	}

	@Override
	public TableEntity findByIdAndStoreIdForUpdate(UUID queryTableId, Long storeId) {
		return queryFactory
			.selectFrom(qTableEntity)
			.where(qTableEntity.id.eq(queryTableId)
				.and(qTableEntity.store.id.eq(storeId)))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE)
			.fetchOne();
	}

	@Override
	public List<TableEntity> findByStoreId(Long storeId) {
		return queryFactory
			.selectFrom(qTableEntity)
			.where(qTableEntity.store.id.eq(storeId)
				.and(qTableEntity.deletedAt.isNull()))
			.fetch();
	}

	@Override
	public boolean existsTableByStoreIdAndTableNumberForUpdate(Long storeId, Integer tableNumberForUpdate) {
		return queryFactory
			.selectOne()
			.from(qTableEntity)
			.where(qTableEntity.store.id.eq(storeId)
				.and(qTableEntity.tableNumber.tableNumber.eq(tableNumberForUpdate)))
			.setLockMode(LockModeType.PESSIMISTIC_WRITE) // TODO : 해당 쿼리는 테이블 전체 락을 유발할 가능성 추후 성능 개선 여지 있음
			.fetchFirst() != null;
	}

	@Override
	public Optional<TableEntity> findTableJoinStoreByTableId(UUID qureyTableId) {
		TableEntity tableEntity = queryFactory
			.selectFrom(qTableEntity)
			.join(qTableEntity.store, qStoreEntity).fetchJoin()
			.where(qTableEntity.id.eq(qureyTableId))
			.fetchOne();
		return Optional.ofNullable(tableEntity);
	}

	@Override
	@Transactional
	public void updateTableInfoById(UUID tableId, Integer updateTableNumber, TablePointVo tablePointVo,
		Integer tableCapacity) {
		long success = queryFactory
			.update(qTableEntity)
			.set(qTableEntity.tableNumber.tableNumber, updateTableNumber)
			.set(qTableEntity.tablePoint.tableX, tablePointVo.getTableX())
			.set(qTableEntity.tablePoint.tableY, tablePointVo.getTableY())
			.set(qTableEntity.capacity, tableCapacity)
			.where(qTableEntity.id.eq(tableId))
			.execute();
		if (success == 0) {
			throw new IllegalArgumentException("테이블 정보 수정에 실패했습니다.");
		}
	}
}
