package domain.pos.call.entity;

import lombok.Getter;

@Getter
public class TableCallInfo {
	private final Long tableId;
	private final Integer tableNumber;
	private final Long receiptId;

	private TableCallInfo(Long tableId, Integer tableNumber, Long receiptId) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.receiptId = receiptId;
	}

	public static TableCallInfo of(Long tableId, Integer tableNumber, Long receiptId) {
		return new TableCallInfo(tableId, tableNumber, receiptId);
	}
}
