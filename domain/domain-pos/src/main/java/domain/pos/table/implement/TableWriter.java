package domain.pos.table.implement;

import java.util.UUID;

import org.springframework.stereotype.Component;

import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import domain.pos.table.entity.TablePoint;
import domain.pos.table.port.required.repository.TableRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TableWriter {
	private final TableRepository tableRepository;

	public Table changeTableActiveStatus(boolean isActive, Table savedInActiveTable) {
		Table changeActiveTable = tableRepository.changeTableActiveStatus(isActive, savedInActiveTable);
		return changeActiveTable;
	}

	public UUID createTable(Store store, Integer tableNumber, TablePoint tablePoint, Integer tableCapacity) {
		return tableRepository.saveTable(store, tableNumber, tablePoint, tableCapacity);
	}

	public void updateTable(Table table, Integer updateTableNumber, TablePoint updateTablePoint, Integer capacity) {
		tableRepository.updateTableInfo(table, updateTableNumber, updateTablePoint, capacity);
	}

	public void deleteTable(Table table) {
		tableRepository.deleteTable(table);
	}
}
