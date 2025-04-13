package domain.pos.receipt.entity;

import domain.pos.member.entity.UserPassport;
import domain.pos.store.entity.Sale;
import domain.pos.table.entity.Table;
import lombok.Getter;

@Getter
public class Receipt {
	private final ReceiptInfo receiptInfo;
	private final UserPassport userPassport;
	private final Table table;
	private final Sale sale;

	private Receipt(ReceiptInfo receiptInfo, UserPassport userPassport, Table table, Sale sale) {
		this.receiptInfo = receiptInfo;
		this.userPassport = userPassport;
		this.table = table;
		this.sale = sale;
	}

	public static Receipt of(
		final ReceiptInfo receiptInfo,
		final UserPassport userPassport,
		final Table table,
		final Sale sale) {
		return new Receipt(
			receiptInfo,
			userPassport,
			table,
			sale
		);
	}
}
