package com.pos.table.mapper;

import java.util.ArrayList;
import java.util.List;

import com.pos.store.entity.StoreEntity;
import com.pos.store.mapper.StoreMapper;
import com.pos.table.entity.TableEntity;
import com.pos.table.vo.TableNumber;
import com.pos.table.vo.TablePointVo;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableMapper {
	public static List<TableEntity> toTableEntities(StoreEntity storeEntity, Integer queryTableNumber,
		Integer tableCapacity) {
		List<TableEntity> tableEntities = new ArrayList<>();
		for (int i = 1; i <= queryTableNumber; i++) {
			tableEntities.add(TableEntity.of(
				TableNumber.from(i),
				TablePointVo.of(0, 0),
				false,
				tableCapacity,
				storeEntity
			));
		}
		return tableEntities;
	}

	public static Table toTable(TableEntity tableEntity, Store responStore) {
		return Table.of(
			tableEntity.getId(),
			tableEntity.getTableNumber().getTableNumber(),
			tableEntity.getIsActive(),
			tableEntity.getTablePoint().toDomain(),
			tableEntity.getCapacity(),
			responStore
		);
	}

	public static Table toTable(TableEntity tableEntity, Long storeId) {
		return Table.of(
			tableEntity.getId(),
			tableEntity.getTableNumber().getTableNumber(),
			tableEntity.getIsActive(),
			tableEntity.getTablePoint().toDomain(),
			tableEntity.getCapacity(),
			Store.of(storeId, null, null, null, null)
		);
	}

	public static TableEntity toTableEntity(Integer tableNumber, TablePoint tablePoint, boolean isActive,
		Integer tableCapacity, Store store) {
		return TableEntity.of(
			TableNumber.from(tableNumber),
			TablePointVo.of(tablePoint.getTableX(), tablePoint.getTableY()),
			isActive,
			tableCapacity,
			StoreEntity.from(store.getStoreId())
		);
	}

	public static Table toTable(TableEntity tableEntity) {
		return Table.of(
			tableEntity.getId(),
			tableEntity.getTableNumber().getTableNumber(),
			tableEntity.getIsActive(),
			tableEntity.getTablePoint().toDomain(),
			tableEntity.getCapacity(),
			StoreMapper.toStore(tableEntity.getStore())
		);
	}
}
