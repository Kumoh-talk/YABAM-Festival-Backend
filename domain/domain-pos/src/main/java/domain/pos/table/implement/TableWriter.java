package domain.pos.table.implement;

import java.util.List;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TableWriter {
	private final TableRepository tableRepository;

	public Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable) {
		Table changeActiveTable = tableRepository.changeTableActiveStatus(isActive, savedInActiveTable);
		return changeActiveTable;
	}

	public List<Table> createTables(Store responStore, Integer queryTableNumber) {
		return tableRepository.createTablesAll(responStore, queryTableNumber);
	}

	public List<Table> modifyTableNum(Store store, Integer queryUpdateTableNumber) {
		return tableRepository.updateTableNum(store, queryUpdateTableNumber);
	}
}
