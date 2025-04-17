package domain.pos.table.entity;

import domain.pos.store.entity.Store;
import lombok.Getter;

@Getter
public class Table {
	private final Long tableId; // 테이블 고유 ID TODO : 해당 값을 UUID로 하여 QR 코드 인식시 사용할지 고민
	private final Integer tableNumber;
	private final Boolean isActive;
	private final Store store;

	private Table(Long tableId, Integer tableNumber, Boolean isActive, Store store) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.isActive = isActive;
		this.store = store;
	}

	public static Table of(Long tableId, Integer tableNumber, Boolean isActive, Store store) {
		return new Table(
			tableId,
			tableNumber,
			isActive,
			store
		);
	}

	public Table changeActiveStatus(boolean isActive) {
		return Table.of(this.tableId, this.tableNumber, isActive, this.store);
	}
}
