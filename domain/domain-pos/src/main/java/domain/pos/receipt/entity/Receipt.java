package domain.pos.receipt.entity;

import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;
import lombok.Getter;

@Getter
public class Receipt {
	private final ReceiptInfo receiptInfo;
	private final Table table;
	private final Sale sale;

	private Receipt(ReceiptInfo receiptInfo, Table table, Sale sale) {
		this.receiptInfo = receiptInfo;
		this.table = table;
		this.sale = sale;
	}

	public static Receipt of(
		final ReceiptInfo receiptInfo,
		final Table table,
		final Sale sale) {
		return new Receipt(
			receiptInfo,
			table,
			sale
		);
	}
}
