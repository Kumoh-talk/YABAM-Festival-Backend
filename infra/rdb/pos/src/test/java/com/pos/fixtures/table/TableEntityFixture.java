package com.pos.fixtures.table;

import java.util.List;

import com.pos.store.entity.StoreEntity;
import com.pos.table.entity.TableEntity;
import com.pos.table.mapper.TableMapper;

public class TableEntityFixture {
	public static List<TableEntity> TABLEENTITY_LIST(Integer tableCount, StoreEntity storeEntity) {
		return TableMapper.toTableEntities(storeEntity, tableCount);
	}
}
