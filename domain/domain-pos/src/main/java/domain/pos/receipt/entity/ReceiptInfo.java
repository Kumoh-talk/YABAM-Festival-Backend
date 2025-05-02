package domain.pos.receipt.entity;

import java.time.Duration;
import java.time.LocalDateTime;

import domain.pos.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReceiptInfo {
	private Long receiptId;
	private boolean isAdjustment;
	private LocalDateTime startUsageTime;
	private LocalDateTime stopUsageTime;
	private Integer occupancyFee;

	@Builder
	public ReceiptInfo(Long receiptId, boolean isAdjustment, LocalDateTime startUsageTime,
		LocalDateTime stopUsageTime, Integer occupancyFee) {
		this.receiptId = receiptId;
		this.isAdjustment = isAdjustment;
		this.startUsageTime = startUsageTime;
		this.stopUsageTime = stopUsageTime;
		this.occupancyFee = occupancyFee;
	}

	public void stop(Store store) {
		this.stopUsageTime = LocalDateTime.now();

		long minutesUsed = Duration.between(this.startUsageTime, this.stopUsageTime).toMinutes();
		Integer costPerMinute = store.getStoreInfo().getTableCost();

		this.occupancyFee = (int)(minutesUsed * costPerMinute);
	}

	public void restart() {
		this.stopUsageTime = null;
		this.occupancyFee = null;
	}

	public void adjust() {
		this.isAdjustment = true;
	}
}
