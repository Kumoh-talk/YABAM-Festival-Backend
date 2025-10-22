package fixtures.receipt.v2;

import java.util.UUID;

import domain.pos.receipt.entity.v2.domain.Receipt;

public class ReceiptFixture {
	public static final UUID VALID_RECEIPT_ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	public static final Long VALID_SALE_ID_1 = 1L;
	public static final UUID VALID_TABLE_ID_1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174111");

	public static Receipt VALID_STARTED_RECEIPT() {
		return Receipt.fromInfra(VALID_RECEIPT_ID_1, UsageTimeFixture.STARTED_USAGE_TIME(), false,
			VALID_SALE_ID_1, VALID_TABLE_ID_1);
	}

	public static Receipt VALID_STOPPED_RECEIPT() {
		return Receipt.fromInfra(VALID_RECEIPT_ID_1, UsageTimeFixture.STOPPED_USAGE_TIME(), false,
			VALID_SALE_ID_1, VALID_TABLE_ID_1);
	}

	public static Receipt VALID_ADJUSTED_RECEIPT() {
		return Receipt.fromInfra(VALID_RECEIPT_ID_1, UsageTimeFixture.STOPPED_USAGE_TIME(), true,
			VALID_SALE_ID_1, VALID_TABLE_ID_1);
	}
}
