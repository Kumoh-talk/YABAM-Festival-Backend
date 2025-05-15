package domain.pos.receipt.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReceiptInfo {
	private UUID receiptId;
	private boolean isAdjustment;
	private LocalDateTime startUsageTime;
	private LocalDateTime stopUsageTime;
	private Integer occupancyFee;

	@Builder
	public ReceiptInfo(UUID receiptId, boolean isAdjustment, LocalDateTime startUsageTime,
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
		Integer unitMinutes = store.getStoreInfo().getTableTime();
		Integer unitCost = store.getStoreInfo().getTableCost();

		int unitCount = (int)Math.ceil((double)minutesUsed / unitMinutes);
		this.occupancyFee = unitCount * unitCost;
	}

	public void setStartUsageTime(LocalDateTime startUsageTime) {
		this.startUsageTime = startUsageTime;
	}
}
