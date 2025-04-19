package domain.pos.table.implement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TableReader {
	private final TableRepository tableRepository;

	public Optional<Table> findLockTableById(Long queryTableId) {
		return tableRepository.findByIdWithLock(queryTableId);
	}

	public boolean isExistsTable(Store responStore) {
		return tableRepository.existsTableByStoreWithLock(responStore);
	}

	public List<Table> findTables(Long storeId) {
		return tableRepository.findTablesByStoreId(storeId);
	}
}
