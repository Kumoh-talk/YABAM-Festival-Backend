package domain.pos.table.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;

@Repository
public interface TableRepository {

	Optional<Table> findByIdWithLock(UUID queryTableId, Long storeId);

	Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable);

	List<Table> findTablesByStoreId(Long storeId);

	boolean existsTableByStoreAndTableNumWithLock(Store store, Integer tableNumber);

	UUID saveTable(Store store, Integer tableNumber, TablePoint tablePoint);

	Optional<Table> findTableWithStoreByTableId(UUID qureyTableId);

	void updateTableInfo(Table table, Integer updateTableNumber, TablePoint updateTablePoint);

	void deleteTable(Table table);
}
