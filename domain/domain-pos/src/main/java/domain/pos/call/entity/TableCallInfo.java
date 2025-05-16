package domain.pos.call.entity;

import java.util.UUID;

import lombok.Getter;

@Getter
public class TableCallInfo {
	private final UUID tableId;
	private final Integer tableNumber;
	private final UUID receiptId;

	private TableCallInfo(UUID tableId, Integer tableNumber, UUID receiptId) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.receiptId = receiptId;
	}

	public static TableCallInfo of(UUID tableId, Integer tableNumber, UUID receiptId) {
		return new TableCallInfo(tableId, tableNumber, receiptId);
	}
}
