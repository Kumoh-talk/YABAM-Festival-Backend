package com.pos.table.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;
import com.pos.table.vo.TablePointVo;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class TableRepositoryImpl implements TableRepository {
	private final TableJpaRepository tableJpaRepository;

	@Override
	public Optional<Table> findByIdWithLock(UUID queryTableId, Long storeId) {
		TableEntity tableEntity = tableJpaRepository.findByIdAndStoreIdForUpdate(queryTableId, storeId);
		if (tableEntity == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(TableMapper.toTable(tableEntity, tableEntity.getStore().getId()));
	}

	@Override
	public Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable) {
		tableJpaRepository.updateTableActiveStatusById(isActive, savedInActiveTable);
		return savedInActiveTable.changeActiveStatus(isActive);
	}

	@Override
	public List<Table> findTablesByStoreId(Long storeId) {
		return tableJpaRepository.findByStoreId(storeId)
			.stream()
			.map(tableEntity -> TableMapper.toTable(tableEntity, storeId))
			.toList();
	}

	@Override
	public boolean existsTableByStoreAndTableNumWithLock(Store store, Integer tableNumber) {
		return tableJpaRepository.existsTableByStoreIdAndTableNumberForUpdate(store.getId(), tableNumber);
	}

	@Override
	public UUID saveTable(Store store, Integer tableNumber, TablePoint tablePoint, Integer tableCapacity) {
		TableEntity tableEntity = TableMapper.toTableEntity(tableNumber, tablePoint, false, tableCapacity, store);
		return tableJpaRepository.save(tableEntity).getId();
	}

	@Override
	public Optional<Table> findTableWithStoreByTableId(UUID qureyTableId) {
		return Optional.ofNullable(
			tableJpaRepository.findTableJoinStoreByTableId(qureyTableId)
				.map(tableEntity -> TableMapper.toTable(tableEntity))
				.orElse(null)
		);
	}

	@Override
	public void updateTableInfo(Table table, Integer updateTableNumber, TablePoint updateTablePoint,
		Integer tableCapacity) {
		tableJpaRepository.updateTableInfoById(
			table.getId(),
			updateTableNumber,
			TablePointVo.of(updateTablePoint.getTableX(), updateTablePoint.getTableY()),
			tableCapacity
		);

	}

	@Override
	public void deleteTable(Table table) {
		tableJpaRepository.deleteById(table.getId());
	}
}
