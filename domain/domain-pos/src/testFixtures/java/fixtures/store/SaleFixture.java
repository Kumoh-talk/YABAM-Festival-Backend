package fixtures.store;

import java.time.LocalDateTime;

import domain.pos.sale.entity.Sale;
import domain.pos.store.entity.Store;

public class SaleFixture {
	private static final Long GENERAL_SALE_ID = 1L;

	private static final LocalDateTime GENERAL_SALE_START_DATETIME = LocalDateTime.of(2025, 1, 1, 0, 0);

	private static final LocalDateTime GENERAL_SALE_END_DATETIME = LocalDateTime.of(2025, 1, 1, 23, 59);

	public static Sale GENERAL_OPEN_SALE(Store store) {
		return Sale.of(
			GENERAL_SALE_ID,
			GENERAL_SALE_START_DATETIME,
			null,
			store
		);
	}

	public static Sale GENERAL_CLOSE_SALE(Store store) {
		return Sale.of(
			GENERAL_SALE_ID,
			GENERAL_SALE_START_DATETIME,
			GENERAL_SALE_END_DATETIME,
			store
		);
	}

	public static Sale OPEN_SALE(Store store) {
		return Sale.of(
			null,
			GENERAL_SALE_START_DATETIME,
			null,
			store
		);
	}

	public static Sale CLOSE_SALE(Store store) {
		return Sale.of(
			null,
			GENERAL_SALE_START_DATETIME,
			GENERAL_SALE_END_DATETIME,
			store
		);
	}
}
