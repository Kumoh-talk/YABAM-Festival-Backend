package fixtures.store;

import java.time.LocalDateTime;

import domain.pos.store.entity.Sale;
import domain.pos.store.entity.Store;

public class SaleFixture {
	// 판매 고유 ID
	private static final Long GENERAL_SALE_ID = 1L;

	// 판매 시작 시간
	private static final LocalDateTime GENERAL_SALE_START_DATETIME = LocalDateTime.of(2025, 1, 1, 0, 0);

	// 판매 마감 시간
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
}
