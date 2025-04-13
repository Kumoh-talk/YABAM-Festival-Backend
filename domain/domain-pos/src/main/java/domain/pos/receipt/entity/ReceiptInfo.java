package domain.pos.receipt.entity;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ReceiptInfo {
	private Long receiptId;
	private boolean isAdjustment;
	private LocalDateTime startTime;
	private LocalDateTime endTime;

	public ReceiptInfo(Long receiptId, boolean isAdjustment, LocalDateTime startTime, LocalDateTime endTime) {
		this.receiptId = receiptId;
		this.isAdjustment = isAdjustment;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public static ReceiptInfo of(
		final Long receiptId,
		final boolean isAdjustment,
		final LocalDateTime startTime,
		final LocalDateTime endTime) {
		return new ReceiptInfo(
			receiptId,
			isAdjustment,
			startTime,
			endTime
		);
	}
}
