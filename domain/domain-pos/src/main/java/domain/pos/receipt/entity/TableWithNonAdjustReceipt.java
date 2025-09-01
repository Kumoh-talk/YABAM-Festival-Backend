package domain.pos.receipt.entity;

import java.util.List;
import java.util.UUID;

import domain.pos.table.entity.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TableWithNonAdjustReceipt {
	private final UUID tableId;
	private final Integer tableNumber;
	private final Boolean isActive;
	private final Receipt nonAdjustReceipt;

	@Builder
	public TableWithNonAdjustReceipt(UUID tableId, Integer tableNumber, Boolean isActive,
		Receipt nonAdjustReceipt) {
		this.tableId = tableId;
		this.tableNumber = tableNumber;
		this.isActive = isActive;
		this.nonAdjustReceipt = nonAdjustReceipt;
	}

	public static TableWithNonAdjustReceipt of(Table table, List<Receipt> receipts) {
		Receipt tableReceipt = receipts.stream()
			.filter(receipt -> receipt.getTable().getId().equals(table.getId()))
			.findFirst()
			.orElse(null);
		return TableWithNonAdjustReceipt.builder()
			.tableId(table.getId())
			.tableNumber(table.getTableNumber())
			.isActive(table.getIsActive())
			.nonAdjustReceipt(tableReceipt)
			.build();
	}
}
