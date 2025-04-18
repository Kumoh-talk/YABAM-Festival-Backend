package domain.pos.table.repository;

import java.util.List;
import java.util.Optional;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;

public interface TableRepository {
	List<Table> createTablesAll(Store responStore, Integer queryTableNumber);

	boolean existsTableByStoreWithLock(Store responStore);

	Optional<Table> findByIdWithLock(Long queryTableId);

	Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable);

	// TODO :테이블 수 조정 시 soft delete로 테이블을 삭제 관리한다면 수 조정시에 어떻게 soft delete된 Table 엔티티를 관리할 것인지?
	void updateTableNum(Store store, Integer queryUpdateTableNumber);

	List<Table> findTablesByStoreId(Long storeId);
}
