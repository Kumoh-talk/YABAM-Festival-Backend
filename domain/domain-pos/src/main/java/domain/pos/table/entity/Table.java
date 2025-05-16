package domain.pos.table.entity;

import java.util.UUID;

import domain.pos.store.entity.Store;
import lombok.Getter;

@Getter
public class Table {
	private final UUID tableId; // 테이블 고유 ID TODO : 해당 값을 UUID로 하여 QR 코드 인식시 사용할지 고민
	private final Boolean isActive;
	private final Integer tableNumber;
	private final TablePoint tablePoint;
	private final Integer tableCapacity;
	private final Store store;

	private Table(UUID tableId, Integer tableNumber, Boolean isActive, TablePoint tablePoint, Integer tableCapacity,
		Store store) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.isActive = isActive;
		this.tablePoint = tablePoint;
		this.tableCapacity = tableCapacity;
		this.store = store;
	}

	public static Table of(UUID tableId, Integer tableNumber, Boolean isActive, TablePoint tablePoint,
		Integer tableCapacity, Store store) {
		return new Table(
			tableId,
			tableNumber,
			isActive,
			tablePoint,
			tableCapacity,
			store
		);
	}

	public Table changeActiveStatus(boolean isActive) {
		return Table.of(this.tableId, this.tableNumber, isActive, this.tablePoint, tableCapacity, this.store);
	}
}
