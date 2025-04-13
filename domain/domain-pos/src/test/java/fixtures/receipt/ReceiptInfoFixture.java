package fixtures.receipt;

import java.time.LocalDateTime;

import domain.pos.receipt.entity.ReceiptInfo;

public class ReceiptInfoFixture {
	// receipt 고유 ID
	public static final Long GENERAL_RECEIPT_ID = 1L;

	// receipt 정산 여부
	private static final boolean RECEIPT_IS_ADJUSTMENT = true;
	private static final boolean RECEIPT_IS_NOT_ADJUSTMENT = false;

	// receipt 시작 시간
	public static final LocalDateTime RECEIPT_START_TIME = LocalDateTime.of(2023, 5, 27, 18, 0, 0);

	// receipt 종료 시간
	public static final LocalDateTime RECEIPT_END_TIME = LocalDateTime.of(2023, 5, 27, 20, 0, 0);

	public static ReceiptInfo NON_ADJUSTMENT_RECEIPT_INFO() {
		return ReceiptInfo.of(
			GENERAL_RECEIPT_ID,
			RECEIPT_IS_NOT_ADJUSTMENT,
			RECEIPT_START_TIME,
			RECEIPT_END_TIME
		);
	}

	public static ReceiptInfo ADJUSTMENT_RECEIPT_INFO() {
		return ReceiptInfo.of(
			GENERAL_RECEIPT_ID,
			RECEIPT_IS_ADJUSTMENT,
			RECEIPT_START_TIME,
			RECEIPT_END_TIME
		);
	}
}
