package com.pos.table.repository.querydsl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.pos.table.entity.TableEntity;
import com.pos.table.vo.TablePointVo;

import domain.pos.table.entity.Table;

@Repository
public interface TableDslRepository {

	void updateTableActiveStatusById(boolean isActive, Table savedInActiveTable);

	TableEntity findByIdAndStoreIdForUpdate(UUID queryTableId, Long storeId);

	List<TableEntity> findByStoreId(Long storeId);

	boolean existsTableByStoreIdAndTableNumberForUpdate(Long storeId, Integer tableNumberForUpdate);

	Optional<TableEntity> findTableJoinStoreByTableId(UUID qureyTableId);

	void updateTableInfoById(UUID tableId, Integer updateTableNumber, TablePointVo of);
}
