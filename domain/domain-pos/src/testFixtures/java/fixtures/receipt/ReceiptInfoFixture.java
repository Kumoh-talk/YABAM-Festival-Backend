package fixtures.receipt;

import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.receipt.entity.ReceiptInfo;

public class ReceiptInfoFixture {
	// receipt 고유 ID
	public static final UUID GENERAL_RECEIPT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

	// receipt 정산 여부
	private static final boolean RECEIPT_IS_ADJUSTMENT = true;
	private static final boolean RECEIPT_IS_NOT_ADJUSTMENT = false;

	// receipt 첫 주문 시작 시간
	public static final LocalDateTime START_USAGE_TIME = LocalDateTime.of(2023, 5, 27, 18, 0, 0);

	// receipt 정산 시간
	public static final LocalDateTime STOP_USAGE_TIME = LocalDateTime.of(2023, 5, 27, 20, 0, 0);

	// 최종 테이블 사용료
	public static final Integer OCCUPANCY_FEE = 10000;

	public static ReceiptInfo NON_ADJUSTMENT_RECEIPT_INFO() {
		return ReceiptInfo.builder()
			.receiptId(GENERAL_RECEIPT_ID)
			.isAdjustment(RECEIPT_IS_NOT_ADJUSTMENT)
			.startUsageTime(START_USAGE_TIME)
			.stopUsageTime(STOP_USAGE_TIME)
			.occupancyFee(null)
			.build();
	}

	public static ReceiptInfo ADJUSTMENT_RECEIPT_INFO() {
		return ReceiptInfo.builder()
			.receiptId(GENERAL_RECEIPT_ID)
			.isAdjustment(RECEIPT_IS_ADJUSTMENT)
			.startUsageTime(START_USAGE_TIME)
			.stopUsageTime(STOP_USAGE_TIME)
			.occupancyFee(OCCUPANCY_FEE)
			.build();
	}
}
