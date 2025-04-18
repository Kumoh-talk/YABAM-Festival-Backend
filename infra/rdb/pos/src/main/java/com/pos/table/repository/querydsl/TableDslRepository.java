package com.pos.table.repository.querydsl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.pos.table.entity.TableEntity;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

@Repository
public interface TableDslRepository {
	List<TableEntity> findTablesByStoreWithLock(Store store);

	void softDeleteTablesGTQueryTableCount(Store store, Integer queryUpdateTableNumber);

	void updateTableActiveStatusById(boolean isActive, Table savedInActiveTable);

	TableEntity findTableByidForUpdate(Long queryTableId);

	boolean existsTableByStoreIdForUpdate(Store responStore);

	List<TableEntity> findByStoreId(Long storeId);
}
