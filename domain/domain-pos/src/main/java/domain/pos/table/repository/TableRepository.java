package domain.pos.table.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

@Repository
public interface TableRepository {
	List<Table> createTablesAll(Store responStore, Integer queryTableNumber);

	boolean existsTableByStoreWithLock(Store responStore);

	Optional<Table> findByIdWithLock(Long queryTableId);

	Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable);

	List<Table> updateTableNum(Store store, Integer queryUpdateTableNumber);

	List<Table> findTablesByStoreId(Long storeId);
}
