package com.pos.table.mapper;

import java.util.ArrayList;
import java.util.List;

import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;
import com.pos.table.vo.TableNumber;
import com.pos.table.vo.TablePointVo;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableMapper {
	public static List<TableEntity> toTableEntities(StoreEntity storeEntity, Integer queryTableNumber) {
		List<TableEntity> tableEntities = new ArrayList<>();
		for (int i = 1; i <= queryTableNumber; i++) {
			tableEntities.add(TableEntity.of(
				TableNumber.from(i),
				TablePointVo.of(0, 0),
				false,
				storeEntity
			));
		}
		return tableEntities;
	}

	public static List<TableEntity> toTableEntities(Store store, Integer previousTableCount, Integer queryTableNumber) {
		List<TableEntity> tableEntities = new ArrayList<>();
		for (int i = previousTableCount + 1; i <= queryTableNumber; i++) {
			tableEntities.add(TableEntity.of(
				TableNumber.from(i),
				TablePointVo.of(0, 0),
				false,
				StoreEntity.from(store.getStoreId())
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
			responStore
		);
	}

	public static Table toTable(TableEntity tableEntity, Long storeId) {
		return Table.of(
			tableEntity.getId(),
			tableEntity.getTableNumber().getTableNumber(),
			tableEntity.getIsActive(),
			tableEntity.getTablePoint().toDomain(),
			Store.of(storeId, null, null, null, null)
		);
	}
}
