package fixtures.receipt;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import fixtures.member.UserFixture;
import fixtures.store.SaleFixture;
import fixtures.store.StoreFixture;
import fixtures.table.TableFixture;

public class ReceiptFixture {
	// Store
	private static Store GENERAL_STORE = StoreFixture.GENERAL_OPEN_STORE();

	// ReceiptInfo
	public static ReceiptInfo GENERAL_ADJUSTMENT_RECEIPT_INFO = ReceiptInfoFixture.ADJUSTMENT_RECEIPT_INFO();
	public static ReceiptInfo GENERAL_NON_ADJUSTMENT_RECEIPT_INFO = ReceiptInfoFixture.NON_ADJUSTMENT_RECEIPT_INFO();
	// UserPassport
	public static UserPassport GENERAL_USER_PASSPORT = UserFixture.GENERAL_USER_PASSPORT();
	// Table
	public static Table GENERAL_TABLE = TableFixture.GENERAL_ACTIVE_TABLE(GENERAL_STORE);
	// Sale
	public static Sale GENERAL_SALE = SaleFixture.GENERAL_OPEN_SALE(GENERAL_STORE);

	public static Receipt CUSTOM_RECEIPT(
		final ReceiptInfo receiptInfo,
		final UserPassport userPassport,
		final Table table,
		final Sale sale) {
		return Receipt.of(
			receiptInfo,
			userPassport,
			table,
			sale
		);
	}

	public static Receipt GENERAL_ADJUSTMENT_RECEIPT() {
		return Receipt.of(
			GENERAL_ADJUSTMENT_RECEIPT_INFO,
			GENERAL_USER_PASSPORT,
			GENERAL_TABLE,
			GENERAL_SALE
		);
	}

	public static Receipt GENERAL_NON_ADJUSTMENT_RECEIPT() {
		return Receipt.of(
			GENERAL_NON_ADJUSTMENT_RECEIPT_INFO,
			GENERAL_USER_PASSPORT,
			GENERAL_TABLE,
			GENERAL_SALE
		);
	}
}
