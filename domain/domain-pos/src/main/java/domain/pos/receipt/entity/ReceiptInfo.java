package domain.pos.receipt.entity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.table.entity.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ReceiptInfo {
	private UUID receiptId;
	private boolean isAdjustment;
	private LocalDateTime startUsageTime;
	private LocalDateTime stopUsageTime;
	private Integer occupancyFee;

	public static final int UNIT_MINUTES = 60;
	public static final int FOUR_TABLE_COST = 4000;
	public static final int SIX_TABLE_COST = 6000;

	@Builder
	public ReceiptInfo(UUID receiptId, boolean isAdjustment, LocalDateTime startUsageTime,
		LocalDateTime stopUsageTime, Integer occupancyFee) {
		this.receiptId = receiptId;
		this.isAdjustment = isAdjustment;
		this.startUsageTime = startUsageTime;
		this.stopUsageTime = stopUsageTime;
		this.occupancyFee = occupancyFee;
	}

	public void stop(Table table) {
		this.stopUsageTime = LocalDateTime.now();

		long minutesUsed = Duration.between(this.startUsageTime, this.stopUsageTime).toMinutes();
		int unitCount = (int)Math.ceil((double)minutesUsed / UNIT_MINUTES);
		if (table.getTableCapacity() == 4) {
			this.occupancyFee = unitCount * FOUR_TABLE_COST;
		} else if (table.getTableCapacity() == 6) {
			this.occupancyFee = unitCount * SIX_TABLE_COST;
		}
	}

	public void setStartUsageTime(LocalDateTime startUsageTime) {
		this.startUsageTime = startUsageTime;
	}
}
