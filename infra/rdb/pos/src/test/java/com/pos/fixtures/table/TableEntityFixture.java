package com.pos.fixtures.table;

import java.util.List;

import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;
import com.pos.table.vo.TableNumber;
import com.pos.table.vo.TablePointVo;

public class TableEntityFixture {
	private static final Integer TABLE_NUMBER = 10;
	private static final Integer TABLE_POINT_X = 0;
	private static final Integer TABLE_POINT_Y = 0;
	private static final TablePointVo GENERAL_TABLEPOINT = TablePointVo.of(TABLE_POINT_X, TABLE_POINT_Y);
	private static final Boolean IS_ACTIVE = false;
	private static final Integer TABLE_CAPACITY = 4;

	public static List<TableEntity> TABLEENTITY_LIST(Integer tableCount, StoreEntity storeEntity) {
		return TableMapper.toTableEntities(storeEntity, tableCount, TABLE_CAPACITY);
	}

	public static TableEntity GENERAL_TABLE_ENTITY(StoreEntity storeEntity) {
		return TableEntity.of(
			TableNumber.from(TABLE_NUMBER),
			GENERAL_TABLEPOINT,
			IS_ACTIVE,
			TABLE_CAPACITY,
			storeEntity
		);
	}
}
