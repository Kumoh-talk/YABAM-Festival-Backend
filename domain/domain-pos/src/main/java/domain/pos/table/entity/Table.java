package domain.pos.table.entity;

import domain.pos.store.entity.Store;
import lombok.Getter;

@Getter
public class Table {
	private final Long tableId; // 테이블 고유 ID TODO : 해당 값을 UUID로 하여 QR 코드 인식시 사용할지 고민
	private final Boolean isActive;
	private final Integer tableNumber;
	private final TablePoint tablePoint;
	private final Store store;

	private Table(Long tableId, Integer tableNumber, Boolean isActive, TablePoint tablePoint, Store store) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.isActive = isActive;
		this.tablePoint = tablePoint;
		this.store = store;
	}

	public static Table of(Long tableId, Integer tableNumber, Boolean isActive, TablePoint tablePoint, Store store) {
		return new Table(
			tableId,
			tableNumber,
			isActive,
			tablePoint,
			store
		);
	}

	public Table changeActiveStatus(boolean isActive) {
		return Table.of(this.tableId, this.tableNumber, isActive, this.tablePoint, this.store);
	}
}
