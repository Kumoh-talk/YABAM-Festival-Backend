package domain.pos.table.implement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TableReader {
	private final TableRepository tableRepository;

	public Optional<Table> findLockTableById(UUID queryTableId, Long storeId) {
		return tableRepository.findByIdWithLock(queryTableId, storeId);
	}

	public List<Table> findTables(Long storeId) {
		return tableRepository.findTablesByStoreId(storeId);
	}

	public boolean isExistsTableByStoreAndTableNumWithLock(Store store, Integer tableNumber) {
		return tableRepository.existsTableByStoreAndTableNumWithLock(store, tableNumber);
	}

	public Optional<Table> findTableWithStoreByTableId(UUID qureyTableId) {
		return tableRepository.findTableWithStoreByTableId(qureyTableId);
	}
}
