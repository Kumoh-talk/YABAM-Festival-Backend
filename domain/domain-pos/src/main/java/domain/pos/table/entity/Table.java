package domain.pos.table.entity;

import static java.util.Objects.*;

import java.util.UUID;

import domain.pos.store.entity.Store;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class Table {
	private UUID id;
	private Boolean isActive;
	private TableNumber tableNumber;
	private TablePoint tablePoint;
	private Integer tableCapacity;
	private Store store;
	private Long storeId;

	private Table(UUID id, Integer tableNumber, Boolean isActive, TablePoint tablePoint, Integer tableCapacity,
		Store store) {
		this.id = id;
		this.tableNumber = TableNumber.of(tableNumber);
		this.isActive = isActive;
		this.tablePoint = tablePoint;
		this.tableCapacity = tableCapacity;
		this.store = store;
	}

	private Table() {
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

	public static Table create(Long storeId, TableInfoRequest request) {
		var table = new Table();
		table.storeId = requireNonNull(storeId);
		table.isActive = false;
		table.tableNumber = TableNumber.of(request.tableNumber());
		table.tablePoint = request.tablePoint();
		table.tableCapacity = request.tableCapacity();

		return table;
	}

	public void modify(TableInfoRequest request) {
		this.tableNumber = TableNumber.of(request.tableNumber());
		this.tablePoint = request.tablePoint();
		this.tableCapacity = request.tableCapacity();
	}

	public Table changeActiveStatus(boolean isActive) {
		return Table.of(this.id, this.tableNumber.value(), isActive, this.tablePoint, tableCapacity, this.store);
	}
}
