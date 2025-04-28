package com.pos.table.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TableRepositoryImpl implements TableRepository {
	private final TableJpaRepository tableJpaRepository;

	@Override
	public List<Table> createTablesAll(Store responStore, Integer queryTableNumber) {
		final StoreEntity storeEntity = StoreMapper.toStoreEntity(responStore.getStoreId());
		final List<TableEntity> tableEntities = TableMapper.toTableEntities(storeEntity, queryTableNumber);
		return tableJpaRepository.saveAll(tableEntities)
			.stream()
			.map(tableEntity -> TableMapper.toTable(tableEntity, responStore))
			.toList();
	}

	@Override
	public boolean existsTableByStoreWithLock(Store responStore) {
		return tableJpaRepository.existsTableByStoreIdForUpdate(
			responStore); // TODO : EXISTS 쿼리가 안되므로 count 와 fetchFirst 둘중 고민했는데 나중 회고 필요
	}

	@Override
	public Optional<Table> findByIdWithLock(Long queryTableId) {
		TableEntity tableEntity = tableJpaRepository.findTableByidForUpdate(queryTableId);
		return Optional.ofNullable(TableMapper.toTable(tableEntity, tableEntity.getStore().getId()));
	}

	@Override
	public Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable) {
		tableJpaRepository.updateTableActiveStatusById(isActive, savedInActiveTable);
		return savedInActiveTable.changeActiveStatus(isActive);
	}

	@Override
	public List<Table> updateTableNum(Store store, Integer queryUpdateTableNumber) {
		List<TableEntity> tableEntities = tableJpaRepository.findTablesByStoreWithLock(store);
		List<Table> tableList = new ArrayList<>();
		tableEntities.stream()
			.map(tableEntity -> TableMapper.toTable(tableEntity, store))
			.forEach(tableList::add);
		if (tableEntities.size() == 0) {
			throw new ServiceException(ErrorCode.TABLE_NOT_FOUND);
		}
		if (tableEntities.size() == queryUpdateTableNumber) {
			throw new ServiceException(ErrorCode.TABLE_NOT_EQUAL_MODIFY);
		}
		if (tableEntities.size() > queryUpdateTableNumber) {
			tableJpaRepository.softDeleteTablesGTQueryTableCount(store, queryUpdateTableNumber);
			return tableList.stream()
				.filter(tableEntity -> tableEntity.getTableNumber() <= queryUpdateTableNumber)
				.toList();
		} else {
			List<TableEntity> addedTableEntities = TableMapper.toTableEntities(store, tableEntities.size(),
				queryUpdateTableNumber);
			tableJpaRepository.saveAll(addedTableEntities);
			addedTableEntities.stream()
				.map(tableEntity -> TableMapper.toTable(tableEntity, store.getStoreId()))
				.forEach(tableList::add);
			return tableList;
		}
	}

	@Override
	public List<Table> findTablesByStoreId(Long storeId) {
		return tableJpaRepository.findByStoreId(storeId)
			.stream()
			.map(tableEntity -> TableMapper.toTable(tableEntity, storeId))
			.toList();
	}
}
