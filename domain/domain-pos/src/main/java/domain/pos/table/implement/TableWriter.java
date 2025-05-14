package domain.pos.table.implement;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
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

	public Long createTable(Store store, Integer tableNumber, TablePoint tablePoint) {
		return tableRepository.saveTable(store, tableNumber, tablePoint);
	}

	public void updateTable(Table table, Integer updateTableNumber, TablePoint updateTablePoint) {
		tableRepository.updateTableInfo(table, updateTableNumber, updateTablePoint);
	}

	public void deleteTable(Table table) {
		tableRepository.deleteTable(table);
	}
}
